import os
import glob
import pandas as pd
import xml.etree.ElementTree as ET

def xml_to_csv(path):
    xmlList = []
    for xmlFile in glob.glob(path + '/*.xml'):
        print(xmlFile)
        tree = ET.parse(xmlFile)
        root = tree.getroot()
        for member in root.findall('object'):
            value = (root.find('filename').text,
                     int(root.find('size')[0].text),
                     int(root.find('size')[1].text),
                     member[0].text,
                     int(member[4][0].text),
                     int(member[4][1].text),
                     int(member[4][2].text),
                     int(member[4][3].text)
                     )
            xmlList.append(value)
    columnName = ['filename', 'width', 'height', 'class', 'xmin', 'ymin', 'xmax', 'ymax']
    xmlDf = pd.DataFrame(xmlList, columns=columnName)
    return xmlDf

def main():
    for folder in ['train', 'test']:
        image_path = os.path.join(os.getcwd(), (folder))
        xmlDf = xml_to_csv(image_path)
        xmlDf.to_csv((folder+'_labels.csv'), index=None)
    print('Successfully converted xml to csv.')

main()