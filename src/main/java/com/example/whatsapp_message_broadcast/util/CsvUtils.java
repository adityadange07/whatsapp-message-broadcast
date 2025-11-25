package com.example.whatsapp_message_broadcast.util;

import com.example.whatsapp_message_broadcast.dto.CsvContact;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static List<CsvContact> parseContacts(InputStream is) throws IOException {

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            List<CsvContact> contacts = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord record : csvRecords) {
                String phone = record.get(0);
                if (phone != null && !phone.isBlank()) {
                    contacts.add(new CsvContact(phone.trim()));
                }
            }

            return contacts;
        }

    }

}
