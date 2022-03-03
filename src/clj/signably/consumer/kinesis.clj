(ns signably.kinesis.consumer
  (:import [software.amazon.kinesis.processor
            ShardRecordProcessorFactory ShardRecordProcessor]))

(deftype SignablyRecordProcessor []
  ShardRecordProcessor
  (initialize [this init-input])
  (processRecords [this records-input])
  (leaseLost [this lost-input])
  (shardEnded [this end-input])
  (shutdownRequested [this shutdown-input]))
