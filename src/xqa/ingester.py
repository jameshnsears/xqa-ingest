import argparse
import datetime
import hashlib
import logging
import os
from uuid import uuid4

import sys
from proton import ConnectionException, Message
from proton.handlers import MessagingHandler
from proton.reactor import Container

from xqa.commons import configuration
from xqa.commons.xml_file_finder import XmlFileFinder


class Ingester(MessagingHandler):
    def __init__(self, xml_files):
        MessagingHandler.__init__(self)
        self._service_id = str(uuid4()).split('-')[0]

        logging.info('%s - %s' % (self.__class__.__name__, self._service_id))
        logging.debug('-p=%s' % configuration.path_to_xml_files)
        logging.debug('-message_broker_host=%s' % configuration.message_broker_host)

        self._populate_messages_to_send_list(xml_files)

    @staticmethod
    def now_timestamp_seconds():
        return (datetime.datetime.now() - datetime.datetime(1970, 1, 1)).total_seconds()

    def on_start(self, event):
        connection = 'amqp://%s:%s@%s:%s/' % (configuration.message_broker_user,
                                              configuration.message_broker_password,
                                              configuration.message_broker_host,
                                              configuration.message_broker_port)

        self.ingester_sender = event.container.create_sender(connection, configuration.message_broker_queue_ingest)

        self.insert_event_sender = event.container.create_sender(connection,
                                                                 configuration.message_broker_queue_db_amqp_insert_event)

    # def _send_files(self):
    #     for i, xml_file in enumerate(self._xml_files):
    #         message = Message(address=configuration.message_broker_queue_ingest,
    #                           correlation_id=str(uuid4()),
    #                           creation_time=Ingester.now_timestamp_seconds(),
    #                           durable=True,
    #                           body=self._xml_file_finder.contents_of_file(xml_file).encode('utf-8'),
    #                           subject=os.path.abspath(xml_file))
    #
    #         message_size = sys.getsizeof(message.body)
    #         logging.info(
    #             '%s,%3s: %9s - creation_time=%14s; address=%s; correlation_id=%s; subject=%s; sha256=%s',
    #             '>',
    #             i,
    #             message_size,
    #             message.creation_time,
    #             message.address,
    #             message.correlation_id,
    #             message.subject,
    #             hashlib.sha256(message.body).hexdigest())
    #
    #         self._insert_event(message, message_size, "START.send")
    #         self.ingester_sender.send(message)
    #         self._insert_event(message, message_size, "END.send")

    # def _insert_event(self, message, message_size, state):
    #     creation_time = Ingester.now_timestamp_seconds()
    #
    #     insert_event = """{ "service_id": "%s", "creation_time": %s, "address": "%s", "correlation_id": "%s", "subject": "%s", "digest": "%s", "message_size": %s", "event": "%s" }""" % \
    #                    ('%s - %s' % (self.__class__.__name__, self._service_id),
    #                     creation_time,
    #                     message.address,
    #                     message.correlation_id,
    #                     message.subject,
    #                     hashlib.sha256(message.body).hexdigest(),
    #                     message_size,
    #                     state)
    #
    #     self.insert_event_sender.send(Message(address=configuration.message_broker_queue_db_amqp_insert_event,
    #                                           correlation_id=str(uuid4()),
    #                                           creation_time=creation_time,
    #                                           durable=True,
    #                                           body=insert_event.encode('utf-8')))

    def on_accepted(self, event):
        if self._finished:
            event.connection.close()

    def on_connection_closed(self, event):
        logging.info('EXIT - on_connection_closed')
        event.connection.close()

    def on_disconnected(self, event):
        logging.info('EXIT - on_disconnected')
        event.connection.close()

    def on_sendable(self, event):
        """
https://qpid.apache.org/releases/qpid-proton-0.20.0/proton/python/examples/simple_send.py.html
        """
        while event.sender.credit and self.messages_to_send:
            logging.debug('credit=%d' % event.sender.credit)

            if self.xml_file_list:
                ## place START message on self.messages_to_send
                # send message
                ## place END message on self.messages_to_send
                # remove from list


            message = Message(address=configuration.queue_ingest,
                              correlation_id=str(uuid4()),
                              creation_time=(datetime.datetime.now() - datetime.datetime(1970, 1, 1)).total_seconds(),
                              durable=True,
                              body=self._contents_of_file(self._xml_files[self._sent_count]).encode('utf-8'),
                              subject=os.path.abspath(self._xml_files[self._sent_count]))

            logging.info(
                '%s,%3s: %9s - {"creation_time":"%14s", "address":"%s", "correlation_id":"%s", "subject":"%s", "sha256":"%s"}',
                '>',
                1 + self._sent_count,
                sys.getsizeof(message.body),
                message.creation_time,
                message.address,
                message.correlation_id,
                message.subject,
                hashlib.sha256(message.body).hexdigest())

            event.sender.send(message)
            self._sent_count += 1

    def on_transport_error(self, event):
        logging.error('%s: %s' % (event.type, event.transport.condition.description))
        raise ConnectionException(event.transport.condition.description)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-message_broker_host', '--message_broker_host', required=True,
                        help='i.e. xqa-message-broker')
    parser.add_argument('-p', '--path', required=False,
                        help='i.e. /xml')
    args = parser.parse_args()
    if args.path:
        configuration.path_to_xml_files = args.path
    configuration.message_broker_host = args.message_broker_host

    try:
        xml_file_finder = XmlFileFinder(configuration.path_to_xml_files)
        Container(Ingester(xml_file_finder.find_files())).run()
    except (XmlFileFinder.FinderException, ConnectionException, KeyboardInterrupt) as exception:
        logging.error(exception)
        exit(-1)
