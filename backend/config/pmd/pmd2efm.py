#!/usr/bin/env python
import sys
import xml.etree.ElementTree as ET


def transform(contents):
    root = ET.fromstring(contents)

    for child in root:
        file_name = child.attrib.get('name')
        for violation in child:
            begin_line = violation.get('beginline')
            begin_column = violation.get('begincolumn')
            message = violation.text.strip()
            print(f"{file_name}:{begin_line}:{begin_column}: {message}")


if __name__ == "__main__":
    transform(sys.stdin.read())
