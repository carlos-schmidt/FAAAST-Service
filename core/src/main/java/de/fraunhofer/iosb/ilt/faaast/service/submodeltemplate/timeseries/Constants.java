/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries;

/**
 * Constants related to SMT TimeSeries.
 */
public class Constants {

    // SMT types & properties
    public static final String TIMESERIES_SUBMODEL_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/1/1/Submodel";
    public static final String SEGMENTS_ID_SHORT = "Segments";
    public static final String SEGMENT_START_TIME_ID_SHORT = "StartTime";
    public static final String SEGMENT_END_TIME_ID_SHORT = "EndTime";
    public static final String EXTERNAL_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/ExternalSegment/1/1";
    public static final String LINKED_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/LinkedSegment/1/1";
    public static final String INTERNAL_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/InternalSegment/1/1";
    public static final String INTERNAL_SEGMENT_RECORDS_ID_SHORT = "Records";
    public static final String RECORD_TIME_ID_SHORT = "Time";
    public static final String RECORD_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Record/1/1";
    // Operation: ReadRecords
    public static final String READ_RECORDS_ID_SHORT = "ReadRecords";
    public static final String READ_RECORDS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/ReadRecords";
    public static final String READ_RECORDS_INPUT_TIMESPAN_ID_SHORT = "Timespan";
    public static final String READ_RECORDS_OUTPUT_RECORDS_ID_SHORT = "Records";
    public static final String READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/TimeSeries/ReadRecords/Records";
    // Operation: ReadSegments
    public static final String READ_SEGMENTS_ID_SHORT = "ReadSegments";
    public static final String READ_SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/ReadSegments";
    public static final String READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT = "Timespan";
    public static final String READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT = "Segments";
    public static final String READ_SEGMENTS_OUTPUT_SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/TimeSeries /ReadSegments/Segments/1/1";

    private Constants() {}
}