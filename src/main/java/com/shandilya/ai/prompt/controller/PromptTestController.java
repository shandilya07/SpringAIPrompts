package com.shandilya.ai.prompt.controller;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
public class PromptTestController {

    @Value("classpath:/prompts/system-top-dj.st")
    private Resource topDj;

    private final OpenAiChatClient chatClient;

    @Autowired
    public PromptTestController(OpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/facts/{topic}")
    public Map<String, String> chat(@PathVariable("topic") String topic) {
        String prompt = "Tell me a random fact about {topic}";
        PromptTemplate template = new PromptTemplate(prompt);
        template.add("topic", topic);
        return Map.of("Fact", chatClient.call(template.render()));
    }

    @GetMapping("/dj/{year}")
    public String topDJ(@PathVariable("year") int year) {

        /*String systemText = """
               Your name is {name} and you are an EDM aficionado and has lots of insights about the art of
               EDM creation. You have deep understanding of usual trends of EDM and why an artist is
               liked by the masses which results to them being the most listened and voted artist of the
               year. You should reply to the user request with this knowledge as one liner information
               which tells why the particular EDM artist was voted world number 1 and what subgenre of EDM
               the particular artist produces music in. You should also include your name in the response 
               like "I am {name} and here is why this artist was voted number 1.
               """;*/

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(topDj);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", "EDM-AF01"));

        String userText = """
                Who was the world number on DJ in the year {year}
                """;

        PromptTemplate userPromptTemplate = new PromptTemplate(userText);
        Message userMessage = userPromptTemplate.createMessage(Map.of("year", year));

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}