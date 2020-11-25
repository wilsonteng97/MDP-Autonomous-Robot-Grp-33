import warnings
import numpy as np
from numba import jit

@jit(nopython=True)
def bound_box_iou(box1, box2) -> float:
    x1 = max(box1[0], box2[0])
    y1 = max(box1[1], box2[1])
    x2 = min(box1[2], box2[2])
    y2 = min(box1[3], box2[3])

    # calculate area of intersection rectangle
    intersectArea = max(0, x2 - x1) * max(0, y2 - y1)

    if intersectArea == 0:
        return 0.0

    # calculate area of prediction and ground-truth rectangles
    box1Area = (box1[2] - box1[0]) * (box1[3] - box1[1])
    box2Area = (box2[2] - box2[0]) * (box2[3] - box2[1])

    iou = intersectArea / float(box1Area + box2Area - intersectArea)
    return iou


def filter_bound_boxes(boxes, scores, labels, weights, thr):
    # create dictionary with boxes stored by its class label
    newBoxes = dict()

    for t in range(len(boxes)):

        if len(boxes[t]) != len(scores[t]):
            print('Error. Length of boxes arrays not equal to length of scores array: {} != {}'.format(len(boxes[t]), len(scores[t])))
            exit()

        if len(boxes[t]) != len(labels[t]):
            print('Error. Length of boxes arrays not equal to length of labels array: {} != {}'.format(len(boxes[t]), len(labels[t])))
            exit()

        for j in range(len(boxes[t])):
            score = scores[t][j]
            if score < thr:
                continue
            label = int(labels[t][j])
            boxSection = boxes[t][j]
            x1 = float(boxSection[0])
            y1 = float(boxSection[1])
            x2 = float(boxSection[2])
            y2 = float(boxSection[3])

            # Box data checks
            if x2 < x1:
                warnings.warn('X2 < X1. Swap them.')
                x1, x2 = x2, x1
            if y2 < y1:
                warnings.warn('Y2 < Y1. Swap them.')
                y1, y2 = y2, y1
            if x1 < 0:
                warnings.warn('X1 < 0. Set to 0.')
                x1 = 0
            if x1 > 1:
                warnings.warn('X1 > 1. Set to 1. Normalize boxes in [0, 1] range.')
                x1 = 1
            if x2 < 0:
                warnings.warn('X2 < 0. Set to 0.')
                x2 = 0
            if x2 > 1:
                warnings.warn('X2 > 1. Set to 1. Normalize boxes in [0, 1] range.')
                x2 = 1
            if y1 < 0:
                warnings.warn('Y1 < 0. Set to 0.')
                y1 = 0
            if y1 > 1:
                warnings.warn('Y1 > 1. Set to 1. Normalize boxes in [0, 1] range.')
                y1 = 1
            if y2 < 0:
                warnings.warn('Y2 < 0. Set to 0.')
                y2 = 0
            if y2 > 1:
                warnings.warn('Y2 > 1. Set to 1. Normalize boxes in [0, 1] range.')
                y2 = 1
            if (x2 - x1) * (y2 - y1) == 0.0:
                warnings.warn("Zero area box skipped: {}.".format(boxSection))
                continue

            box = [int(label), float(score) * weights[t], x1, y1, x2, y2]
            if label not in newBoxes:
                newBoxes[label] = []
            newBoxes[label].append(box)

    # sort each list by score
    for newBox in newBoxes:
        currentBoxes = np.array(newBoxes[newBox])
        newBoxes[k] = currentBoxes[currentBoxes[:, 1].argsort()[::-1]]

    return newBoxes


def get_weighted_box(boxes, conf_type='avg'):
    box = np.zeros(6, dtype=np.float32)
    confidence = 0
    confidenceList = []
    for b in boxes:
        box[2:] += (b[1] * b[2:])
        confidence += b[1]
        confidenceList.append(b[1])
    box[0] = boxes[0][0]
    if conf_type == 'avg':
        box[1] = confidence / len(boxes)
    elif conf_type == 'max':
        box[1] = np.array(confidenceList).max()
    box[2:] /= confidence
    return box


def get_matching_box(boxesList, newBox, matchIou):
    bestIou = matchIou
    bestIndex = -1
    for i in range(len(boxesList)):
        box = boxesList[i]
        if box[0] != newBox[0]:
            continue
        iou = bound_box_iou(box[2:], newBox[2:])
        if iou > bestIou:
            bestIndex = i
            bestIou = iou
    return bestIndex, bestIou


def wbf(boxesList, scoresList, labelsList, weights=None, iou_thr=0.1, skip_box_thr=0.9, conf_type='avg', allows_overflow=False):
    if weights is None:
        weights = np.ones(len(boxesList))
    if len(weights) != len(boxesList):
        print('Incorrect number of weights {}. Must be: {}. Set weights equal 1.'.format(len(weights), len(boxesList)))
        weights = np.ones(len(boxesList))
    weights = np.array(weights)

    if conf_type not in ['avg', 'max']:
        print('Unknown conf_type: {}. Must be "avg" or "max"'.format(conf_type))
        exit()

    filterBoxes = filter_bound_boxes(boxesList, scoresList, labelsList, weights, skip_box_thr)
    if len(filterBoxes) == 0:
        return np.zeros((0, 4)), np.zeros((0,)), np.zeros((0,))

    allBoxes = []
    for label in filterBoxes:
        boxes = filterBoxes[label]
        newBoxes = []
        weightedBoxes = []

        # clusterize boxes
        for j in range(0, len(boxes)):
            index, best_iou = get_matching_box(weightedBoxes, boxes[j], iou_thr)
            if index != -1:
                newBoxes[index].append(boxes[j])
                weightedBoxes[index] = get_weighted_box(newBoxes[index], conf_type)
            else:
                newBoxes.append([boxes[j].copy()])
                weightedBoxes.append(boxes[j].copy())

        # rescale confidence based on number of models and boxes
        for i in range(len(newBoxes)):
            if not allows_overflow:
                weightedBoxes[i][1] = weightedBoxes[i][1] * min(weights.sum(), len(newBoxes[i])) / weights.sum()
            else:
                weightedBoxes[i][1] = weightedBoxes[i][1] * len(newBoxes[i]) / weights.sum()
        allBoxes.append(np.array(weightedBoxes))

    allBoxes = np.concatenate(allBoxes, axis=0)
    allBoxes = allBoxes[allBoxes[:, 1].argsort()[::-1]]
    boxes = allBoxes[:, 2:]
    scores = allBoxes[:, 1]
    labels = allBoxes[:, 0]
    return boxes, scores, labels