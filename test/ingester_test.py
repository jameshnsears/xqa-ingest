import os

import pytest
from pytest import raises

from xqa.ingester import Ingester


@pytest.fixture
def path_to_good_test_data():
    return os.path.join('%s/resources/test-data/good' % os.path.dirname(__file__))


def test_find_xml_files_in_path(path_to_good_test_data):
    ingester = Ingester(path_to_good_test_data)
    assert len(ingester._find_xml_files()) == 1


def test_no_xml_files_found():
    path_to_non_existant_test_data = '/dev/null'
    with raises(Ingester.IngestException, message=Ingester.ERROR_NO_XML_FILES_FOUND):
        ingester = Ingester(path_to_non_existant_test_data)
        ingester._find_xml_files()


def test_file_contents_not_well_formed():
    with raises(Ingester.IngestException, message=Ingester.ERROR_NO_XML_FILES_FOUND):
        path_to_not_well_formed_test_data = os.path.join(os.path.dirname(__file__),
                                                         'resources/test-data/bad/not_well_formed')
        ingester = Ingester(path_to_not_well_formed_test_data)
        ingester._find_xml_files()


def test_file_has_wrong_mimetype():
    with raises(Ingester.IngestException, message=Ingester.ERROR_FILE_MIMETYPE):
        ingester = Ingester(os.path.dirname(__file__))
        ingester._can_file_be_used(os.path.join(os.path.dirname(__file__), 'ingester_test.py'), 'ingester_test.py')
