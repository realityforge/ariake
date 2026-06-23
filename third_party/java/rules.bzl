load("@rules_java//java:defs.bzl", _java_binary = "java_binary", _java_library = "java_library", _java_test = "java_test")

_JAVA_RELEASE = "25"
_JSPECIFY = "//third_party/java:jspecify"
_NULLAWAY_PLUGIN = "//third_party/java:nullaway_plugin"

_JAVA_JAVACOPTS = [
    "--release",
    _JAVA_RELEASE,
    "-Werror",
    "-Asting.warnings_as_errors=true",
    "-Xep:NullAway:ERROR",
    "-Xep:RequireExplicitNullMarking:ERROR",
    "-XepOpt:NullAway:OnlyNullMarked=true",
    "-Xlint:all,-processing,-serial,-path,-options,-classfile,-this-escape",
]

_JAVA_TEST_JVM_FLAGS = [
    "-ea",
]

def _with_jspecify(deps):
    return [_JSPECIFY] + deps if _JSPECIFY not in deps else deps

def _with_nullaway(plugins):
    return [_NULLAWAY_PLUGIN] + plugins if _NULLAWAY_PLUGIN not in plugins else plugins

def _has_sources(srcs):
    return len(srcs) > 0

def java_library(name, srcs = [], javacopts = [], deps = [], plugins = [], **kwargs):
    nullaway_enabled = _has_sources(srcs)
    _java_library(
        name = name,
        srcs = srcs,
        deps = _with_jspecify(deps) if nullaway_enabled else deps,
        javacopts = _JAVA_JAVACOPTS + javacopts,
        plugins = _with_nullaway(plugins) if nullaway_enabled else plugins,
        **kwargs
    )

def java_binary(name, srcs = [], javacopts = [], deps = [], plugins = [], **kwargs):
    nullaway_enabled = _has_sources(srcs)
    _java_binary(
        name = name,
        srcs = srcs,
        deps = _with_jspecify(deps) if nullaway_enabled else deps,
        javacopts = _JAVA_JAVACOPTS + javacopts,
        plugins = _with_nullaway(plugins) if nullaway_enabled else plugins,
        **kwargs
    )

def java_test(name, srcs = [], javacopts = [], deps = [], plugins = [], jvm_flags = [], **kwargs):
    nullaway_enabled = _has_sources(srcs)
    _java_test(
        name = name,
        srcs = srcs,
        deps = _with_jspecify(deps) if nullaway_enabled else deps,
        javacopts = _JAVA_JAVACOPTS + javacopts,
        plugins = _with_nullaway(plugins) if nullaway_enabled else plugins,
        jvm_flags = _JAVA_TEST_JVM_FLAGS + jvm_flags,
        **kwargs
    )
