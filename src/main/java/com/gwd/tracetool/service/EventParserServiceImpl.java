package com.gwd.tracetool.service;

import com.gwd.tracetool.component.ToolProperties;
import com.gwd.tracetool.domain.EventModel;
import com.gwd.tracetool.utils.JSONUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventParserServiceImpl implements EventParserService {

    private final ToolProperties toolProperties;

    @Override
    public List readEventList(String date) {

        int demsHost = 1;
        List<EventModel> list = new ArrayList<EventModel>();
        String fileName = generateFileName(date);

        List<Path> dagsLogDirPathList = new ArrayList<>(Arrays.asList(
                Paths.get(toolProperties.getDems1LogPath(), fileName),
                Paths.get(toolProperties.getDems2LogPath(), fileName)));

        for (Path logPath : dagsLogDirPathList) {
            list.addAll(readEventLogFile(logPath, demsHost++));
        }

        Collections.sort(list);
        return list;
    }

    private List readEventLogFile(Path path, int demsHost) {
        List<EventModel> list = new ArrayList<EventModel>();
        JSONObject headerJSON = new JSONObject();
        File file = new File(path.toString());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String token = "receivemessage: ";
            String queueRecord = "receive message propertioes: MessageProperties";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


            while ((line = br.readLine()) != null) {
                if (line.contains(queueRecord)) {

                    int i = line.indexOf(token) + token.length();
                    if (i != -1) {

                        LocalDateTime occurrenceTime = LocalDateTime.parse(line.substring(0, 23), formatter);
                        String subStr = line.substring(i);

                        headerJSON = stringToJSON(subStr);
                        EventModel eventModel = createEventModel(headerJSON);
                        eventModel.setOccurrenceTime(occurrenceTime);
                        eventModel.setDemsHost(demsHost);
                        list.add(eventModel);

                    }
                }
            }
        } catch (IOException e) {
            log.info("Fail to parse log : log-path={}, stack-trace={}", path, new Throwable().getStackTrace());
        }

        return list;
    }

    private EventModel createEventModel(JSONObject headerJson) {
        String eventName = JSONUtils.getEventName(headerJson);
        String transactionId = JSONUtils.getTransactionId(headerJson);
        String workflowType = JSONUtils.getWorkflowType(headerJson);
        String createAt = JSONUtils.getCreateAt(headerJson);
        String provider = JSONUtils.getProvider(headerJson);

        ZonedDateTime createAtZonedTime = ZonedDateTime.parse(createAt);
        LocalDateTime createAtLocalTime = createAtZonedTime.toLocalDateTime();

        return EventModel.builder()
                .eventName(eventName)
                .transactionId(transactionId)
                .workflowType(workflowType)
                .provider(provider)
                .createAt(createAtLocalTime)
                .build();
    }

    private String generateFileName(String date) {
        // example : dems.2022-07-14.log
        SimpleDateFormat dateFormatParser = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatParser.setLenient(false);
        try {
            // 대상 인자 검증
            dateFormatParser.parse(date);
        } catch (java.text.ParseException e) {
            log.info("Fail to parse log : log-path={}, stack-trace={}", date, new Throwable().getStackTrace());
        }
        return String.format("dems.%s.log", date);
    }

    private JSONObject stringToJSON(String strValue) {
        JSONParser parser = new JSONParser();

        try {
            return (JSONObject) parser.parse(strValue);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
