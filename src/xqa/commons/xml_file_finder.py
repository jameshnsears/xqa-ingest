import logging
import mimetypes
import os

from lxml import etree
from lxml.etree import XMLSyntaxError


class XmlFileFinder:
    class FinderException(Exception):

        def __init__(self, message=None):
            if message:
                self.message = message

    ERROR_NO_XML_FILES_FOUND = 'no XML files found'
    ERROR_FILE_MIMETYPE = 'incorrect mimetype'
    ERROR_FILE_CONTENTS_NOT_WELL_FORMED = 'file not well-formed'

    def __init__(self, path_to_xml_candidate_files):
        self._path_to_xml_candidate_files = path_to_xml_candidate_files

    def find_files(self):
        xml_files = []

        for root, _, filenames in os.walk(self._path_to_xml_candidate_files):
            for filename in filenames:
                path_to_filename = self._full_path_to_file(root, filename)
                try:
                    if self.can_file_be_used(path_to_filename, filename):
                        xml_files.append(path_to_filename)
                except XmlFileFinder.FinderException:
                    pass

        if not xml_files:
            logging.warning(XmlFileFinder.ERROR_NO_XML_FILES_FOUND)
            raise XmlFileFinder.FinderException(XmlFileFinder.ERROR_NO_XML_FILES_FOUND)

        return sorted(xml_files)

    def _rm_bom_from_file_contents(self, path_to_filename):
        file_contents = open(path_to_filename, mode='r', encoding='utf-8-sig').read()
        open(path_to_filename, mode='w', encoding='utf-8').write(file_contents)

    def _full_path_to_file(self, root, filename):
        return os.path.join(root, filename)

    def can_file_be_used(self, path_to_filename, filename):
        if not self._check_file_mimetype_recognised(path_to_filename):
            logging.debug('N: %s: %s' % (XmlFileFinder.ERROR_FILE_MIMETYPE, filename))
            raise XmlFileFinder.FinderException(XmlFileFinder.ERROR_FILE_MIMETYPE)

        if not self._check_file_contents_well_formed(path_to_filename):
            logging.debug('N: %s: %s' % (XmlFileFinder.ERROR_FILE_CONTENTS_NOT_WELL_FORMED, filename))
            raise XmlFileFinder.FinderException(XmlFileFinder.ERROR_FILE_CONTENTS_NOT_WELL_FORMED)

        self._rm_bom_from_file_contents(path_to_filename)

        logging.debug('Y: %s' % path_to_filename)
        return True

    def _check_file_contents_well_formed(self, path_to_filename):
        try:
            etree.parse(path_to_filename)
            return True
        except XMLSyntaxError:
            return False

    def _check_file_mimetype_recognised(self, path_to_filename):
        if mimetypes.guess_type(path_to_filename) in [('application/xml', None), ('text/xml', None)]:
            return True
        return False

    def contents_of_file(self, xml_file):
        with open(xml_file) as f:
            return f.read()
