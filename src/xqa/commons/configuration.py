import logging
import socket
import sys

path_to_xml_files = '/opt/xqa-ingest/xml'

message_broker_host = socket.gethostbyname(socket.gethostname())
message_broker_port = 5672
message_broker_user = 'admin'
message_broker_password = 'admin'
message_broker_queue_db_amqp_insert = 'queue://xqa.db.amqp.insert'
message_broker_topic_cmd_stop = 'topic://xqa.cmd.stop'

queue_ingest = 'queue://xqa.ingest'

topic_cmd_stop = 'topic://xqa.cmd.stop'

storage_host = socket.gethostbyname(socket.gethostname())
storage_port = 5432
storage_user = 'xqa'
storage_password = 'xqa'
storage_database_name = 'xqa'

logging.basicConfig(stream=sys.stdout,
                    level=logging.INFO,
                    format="%(asctime)s  %(levelname)8s --- [%(threadName)20s]: %(funcName)25s, %(lineno)3s: %(message)s")
