import logging
import socket
import sys

path_to_xml_files = '/xml'

message_broker_host = socket.gethostbyname(socket.gethostname())
message_broker_port = 5672
message_broker_user = 'admin'
message_broker_password = 'admin'
message_broker_queue_ingest = 'queue://xqa.ingest'
message_broker_queue_db_amqp_insert_event = 'queue://xqa.db.amqp.insert_event'

storage_host = socket.gethostbyname(socket.gethostname())
storage_port = 5432
storage_user = 'xqa'
storage_password = 'xqa'
storage_database_name = 'xqa'

logging.basicConfig(stream=sys.stdout,
                    level=logging.DEBUG,
                    format="%(asctime)s  %(levelname)8s --- [%(threadName)20s]: %(funcName)25s, %(lineno)3s: %(message)s")
