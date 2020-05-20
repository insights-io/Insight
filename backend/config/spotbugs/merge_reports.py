#!/usr/bin/env python
import xml.etree.ElementTree as ET
from pathlib import Path


def transform(contents):
    root = ET.fromstring(contents)

    project = root.findall('Project')[0]
    source_dir = project.findall('SrcDir')[1].text

    for child in root:
        if child.tag == 'BugInstance':
            type = child.get('type')
            message = child.findall("ShortMessage")[0].text
            source_line = child.findall("SourceLine")[0]
            begin_line = source_line.get('start')
            file_name = source_line.get('sourcepath')
            print(f"{source_dir}/{file_name}:{begin_line}: {type}({message})")


if __name__ == "__main__":
    for path in Path('backend').rglob('**/build/reports/spotbugs/main.xml'):
        with open(path, 'r') as report_file:
            transform(report_file.read())
