package org.ariake.examples.hello;

import sting.server.Transactional;

@Transactional
public interface HealthResponder {
    String health();
}
