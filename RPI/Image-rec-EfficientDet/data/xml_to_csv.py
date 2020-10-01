import os
import glob
import pandas as pd
import xml.etree.ElementTree as ET

data_dict = {

}

COLUMN_NAME = ['image_id', 
                'width', 
                'height', 
                'class', 
                'xmin', 
                'ymin', 
                'xmax', 
                'ymax', 
                'area']

def get_area(xmin, ymin, xmax, ymax):
    return (xmax-xmin) * (ymax-ymin)

def xml_to_csv(path):
    xml_list = []
    for xml_file in glob.glob(path + '/*.xml'):
        tree = ET.parse(xml_file)
        root = tree.getroot()
        for member in root.findall('object'):
            xmin, ymin = int(member[4][0].text), int(member[4][1].text)
            xmax, ymax = int(member[4][2].text), int(member[4][3].text)


            value = (root.find('filename').text,
                    int(root.find('size')[0].text),
                    int(root.find('size')[1].text),
                    member[0].text,
                    xmin,
                    ymin,
                    xmax,
                    ymax,
                    get_area(xmin, ymin, xmax, ymax)
                    )
            xml_list.append(value) 
    xml_df = pd.DataFrame(xml_list, columns=COLUMN_NAME)
    return xml_df


def main():
    for folder in ['train', 'test']:
        image_path = os.path.join(os.getcwd(), folder)
        xml_df = xml_to_csv(image_path)
        xml_df.to_csv(('csv/'+folder+'.csv'), index=None)
    print('Successfully converted xml to csv.')

if __name__ == "__main__":
    main()