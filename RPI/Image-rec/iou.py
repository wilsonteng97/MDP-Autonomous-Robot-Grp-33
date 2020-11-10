def calculate_iou(box1, box2):
	# find coordinates of the intersection rectangle
	x1 = max(box1[0], box2[0])
	y1 = max(box1[1], box2[1])
	x2 = min(box1[2], box2[2])
	y2 = min(box1[3], box2[3])

	# calculate area of intersection rectangle
	intersectArea = max(0, x2 - x1 + 1) * max(0, y2 - y1 + 1)

	# calculate area of prediction and ground-truth rects
	box1Area = (box1[2] - box1[0] + 1) * (box1[3] - box1[1] + 1)
	box2Area = (box2[2] - box2[0] + 1) * (box2[3] - box2[1] + 1)

	# compute iou
	iou = intersectArea / float(box1Area + box2Area - intersectArea)

	return iou