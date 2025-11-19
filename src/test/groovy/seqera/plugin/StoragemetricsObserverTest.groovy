package seqera.plugin

import nextflow.Session
import spock.lang.Specification

/**
 * Implements a basic factory test
 *
 */
class StoragemetricsObserverTest extends Specification {

    def 'should create the observer instance' () {
        given:
        def factory = new StoragemetricsFactory()
        when:
        def result = factory.create(Mock(Session))
        then:
        result.size() == 1
        result.first() instanceof StoragemetricsObserver
    }

}
