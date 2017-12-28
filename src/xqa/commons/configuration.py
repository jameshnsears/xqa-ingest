import logging
import socket
import sys

url_amqp = 'amqp://admin:admin@%s:5672/' % socket.gethostbyname(socket.gethostname())

queue_ingest = 'queue://xqa.ingest'

topic_cmd_stop = 'topic://xqa.cmd.stop'

logging.basicConfig(stream=sys.stdout,
                    level=logging.INFO,
                    format="%(asctime)s  %(levelname)8s --- [%(threadName)20s]: %(funcName)25s, %(lineno)3s: %(message)s")
