include $JTESTS/smoketest/perf/common.spec
;
statspec createsPerSecond * cacheperf.CachePerfStats * creates
filter=perSecond combine=combineAcrossArchives ops=mean? trimspec=creates
;
statspec destroysPerSecond * CachePerfStats cachePerfStats destroys
filter=perSecond combine=combineAcrossArchives ops=mean? trimspec=creates
;
