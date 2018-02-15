import logging
import socket
import sys

url_amqp = 'amqp://admin:admin@%s:5672/' % socket.gethostbyname(socket.gethostname())

queue_ingest = 'queue://xqa.ingest'

topic_cmd_stop = 'topic://xqa.cmd.stop'

storage_host = '127.0.0.1'
storage_port = 5432
storage_user = 'xqa'
storage_password = 'xqa'
storage_database_name = 'xqa'

logging.basicConfig(stream=sys.stdout,
                    level=logging.INFO,
                    format="%(asctime)s  %(levelname)8s --- [%(threadName)20s]: %(funcName)25s, %(lineno)3s: %(message)s")
