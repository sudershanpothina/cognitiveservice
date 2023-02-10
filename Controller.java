package com.sdrshn.demo1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.core.util.polling.SyncPoller;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedIterable;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;



import java.util.StringTokenizer;  
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {

    @GetMapping("/getProperCase") 
    public String convert(@RequestParam String inputString) {
        String KEY = "";
        String ENDPOINT = "";
        TextAnalyticsClient client = authenticateClient(KEY, ENDPOINT);
        // return recognizeEntitiesExample(client, inputString.toLowerCase());
        return syncPoller(client, inputString);

    }

    private TextAnalyticsClient authenticateClient(String key, String endpoint) {
        return new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildClient();
    }

    // private String recognizeEntitiesExample(TextAnalyticsClient client, String inputString)
    // {
    //     String outputText = "";

    //     for (CategorizedEntity entity : client.recognizeEntities(inputString)) {
    //         System.out.printf("Recognized entity: %s, entity category: %s, confidence score: %f.%n, offset %s \n",
    //      entity.getText(), entity.getCategory(), entity.getConfidenceScore(), entity.getOffset());
    //         outputText = outputText +" "+ convertCase(entity.getText(), entity.getCategory().toString());
    //     }

    //     return outputText.trim();
    // }
    

    private String syncPoller(TextAnalyticsClient client, String inputString) {

        List<String> documents = new ArrayList<>();
        documents.add(inputString.toLowerCase());
        List<String> outputText = new ArrayList<>();

        SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable> syncPoller =
            client.beginRecognizeCustomEntities(documents,
                "test",
                "test",
                "en",
                null);
        syncPoller.waitForCompletion();
        syncPoller.getFinalResult().forEach(documentsResults -> {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (RecognizeEntitiesResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    for (CategorizedEntity entity : documentResult.getEntities()) {
                        System.out.printf(
                            "\tText: %s, category: %s, confidence score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                        outputText.add(convertCase(entity.getText(), entity.getCategory().toString()));
                    }
                } else {
                    System.out.printf("\tCannot recognize custom entities. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        });
        return  arrayToString(outputText);
    }
    private String convertCase(String inputValue, String category) {
        return switch (category) {
            case "Organization" -> camelCaseWord(inputValue);
            case "Location" -> camelCaseWord(inputValue);
            case "Person" -> camelCaseWord(inputValue);
            case "Name" -> camelCaseWord(inputValue);
            default -> inputValue;
        };
    }

    private String camelCaseWord(String inputString) {
        System.out.println(inputString);
        StringTokenizer st = new StringTokenizer(inputString);
        if(st.countTokens() > 1) {
            String name = "";
            while(st.hasMoreTokens()){
                String token = st.nextToken();
                System.out.println(token);
                name = name+ " " + Character.toUpperCase(token.charAt(0)) + token.substring(1).toLowerCase();
            }
            return name;
        }
        return Character.toUpperCase(inputString.charAt(0)) + inputString.substring(1).toLowerCase();
    }
    private String arrayToString (List<String> inputArray) {
        String outputText = "";
        for (String value : inputArray) {
            outputText = outputText +" "+ value;
        }
        return outputText;
    }
}
