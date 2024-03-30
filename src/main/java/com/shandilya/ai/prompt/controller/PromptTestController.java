package com.shandilya.ai.prompt.controller;

import com.shandilya.ai.prompt.model.TopDJ;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
public class PromptTestController {

    private final ChatClient chatClient;

    @Autowired
    public PromptTestController(ChatClient chatClient) {
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
    public TopDJ topDJ(@PathVariable("year") int year) {
        BeanOutputParser<TopDJ> parser = new BeanOutputParser<>(TopDJ.class);
        String promptString = """
                Who was the world number on DJ in the year {year}
                {format}
                """;

        //ChatResponse chatResponse = chatClient.call(approachOne(promptString, year, parser));
        ChatResponse chatResponse = chatClient.call(approachTwo(promptString, year, parser));

        Generation generation = chatResponse.getResult();
        return parser.parse(generation.getOutput().getContent());
    }

    private Prompt approachTwo(final String string, int year, BeanOutputParser<?> parser) {
        PromptTemplate template = new PromptTemplate(string, Map.of("year",year, "format", parser.getFormat()));
        return template.create();
    }

    private Prompt approachOne(final String string, int year, BeanOutputParser<?> parser) {
        PromptTemplate template = new PromptTemplate(string);
        template.add("year", year);
        template.add("format", parser.getFormat());
        template.setOutputParser(parser);
        return template.create();
    }
}
