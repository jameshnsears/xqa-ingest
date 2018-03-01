import os

import pytest
from pytest import raises

from xqa.commons.xml_file_finder import XmlFileFinder


@pytest.fixture
def path_to_good_test_data():
    return os.path.join('%s/resources/test-data/good' % os.path.dirname(__file__))


def test_find_xml_files_in_path(path_to_good_test_data):
    xml_file_finder = XmlFileFinder(path_to_good_test_data)
    assert len(xml_file_finder.find_files()) == 1


def test_no_xml_files_found():
    with raises(XmlFileFinder.FinderException, message=XmlFileFinder.ERROR_NO_XML_FILES_FOUND):
        xml_file_finder = XmlFileFinder('/dev/null')
        xml_file_finder.find_files()


def test_file_contents_not_well_formed():
    with raises(XmlFileFinder.FinderException, message=XmlFileFinder.ERROR_NO_XML_FILES_FOUND):
        path_to_not_well_formed_test_data = os.path.join(os.path.dirname(__file__),
                                                         'resources/test-data/bad/not_well_formed')
        xml_file_finder = XmlFileFinder(path_to_not_well_formed_test_data)
        xml_file_finder.find_files()


def test_file_has_wrong_mimetype():
    with raises(XmlFileFinder.FinderException, message=XmlFileFinder.ERROR_FILE_MIMETYPE):
        xml_file_finder = XmlFileFinder(os.path.dirname(__file__))
        xml_file_finder.can_file_be_used(os.path.join(os.path.dirname(__file__), 'ingester_test.py'),
                                         'ingester_test.py')
