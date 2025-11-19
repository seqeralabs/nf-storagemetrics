package seqera.plugin

import nextflow.Session
import spock.lang.Specification

/**
 * Tests for StoragemetricsFactory and StoragemetricsObserver
 *
 */
class StoragemetricsObserverTest extends Specification {

    def 'should create the observer instance' () {
        given:
        def factory = new StoragemetricsFactory()
        def session = Mock(Session) {
            getConfig() >> [storagemetrics: [:]]
            getRunName() >> 'test_run'
            getUniqueId() >> UUID.fromString('12345678-1234-1234-1234-123456789abc')
        }

        when:
        def result = factory.create(session)

        then:
        result.size() == 1
        result.first() instanceof StoragemetricsObserver
    }

    def 'should create config with default filename including run name and unique ID' () {
        given:
        def session = Mock(Session) {
            getConfig() >> [:]
            getRunName() >> 'amazing_tesla'
            getUniqueId() >> UUID.fromString('abcd1234-5678-90ef-1234-567890abcdef')
        }

        when:
        def config = StorageMetricsConfig.fromSession(session)

        then:
        config.fileName == 'storage-metrics.amazing_tesla.abcd1234.txt'
        config.overwrite == true
    }

    def 'should create config with custom values' () {
        given:
        def session = Mock(Session) {
            getConfig() >> [
                storagemetrics: [
                    file: 'custom-metrics.tsv',
                    overwrite: false
                ]
            ]
            getRunName() >> 'test_run'
            getUniqueId() >> UUID.fromString('12345678-1234-1234-1234-123456789abc')
        }

        when:
        def config = StorageMetricsConfig.fromSession(session)

        then:
        config.fileName == 'custom-metrics.tsv'
        config.overwrite == false
    }

    def 'should handle missing run name and unique ID' () {
        given:
        def session = Mock(Session) {
            getConfig() >> [:]
            getRunName() >> null
            getUniqueId() >> null
        }

        when:
        def config = StorageMetricsConfig.fromSession(session)

        then:
        config.fileName == 'storage-metrics.unknown.unknown.txt'
    }

}
