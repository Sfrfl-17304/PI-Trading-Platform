package com.example.predictionservice.strategy;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Batchifier;
import com.example.predictionservice.model.Signal;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Component
public class MachineLearningStrategy implements PredictionStrategy {

    @Value("${ml.model.window-size:14}")
    private int windowSize;

    @Value("${ml.model.path:classpath:models/prediction_model.pt}")
    private Resource modelResource;

    private ZooModel<double[], Float> model;
    private Predictor<double[], Float> predictor;

    @PostConstruct
    public void init() throws Exception {
        // Extract model from classpath to a temporary file for DJL to load
        Path tempModelPath = Files.createTempFile("prediction_model", ".pt");
        tempModelPath.toFile().deleteOnExit();
        
        try (InputStream is = modelResource.getInputStream()) {
            Files.copy(is, tempModelPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Failed to load model file. Machine Learning strategy will fallback to HOLD. " + e.getMessage());
            return; // Skip loading if model isn't present yet
        }

        Criteria<double[], Float> criteria = Criteria.builder()
                .setTypes(double[].class, Float.class)
                .optModelPath(tempModelPath)
                .optTranslator(new PriceSequenceTranslator(windowSize))
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
    }

    @PreDestroy
    public void cleanup() {
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }

    @Override
    public Signal calculateSignal(String symbol, List<Double> prices) {
        if (predictor == null || prices.size() < windowSize) {
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }

        // Extract the latest window
        List<Double> recentPrices = prices.subList(prices.size() - windowSize, prices.size());
        double[] input = recentPrices.stream().mapToDouble(Double::doubleValue).toArray();

        try {
            // Assume the model predicts a score between 0.0 and 1.0
            Float predictionScore = predictor.predict(input);

            String action;
            double confidence;

            if (predictionScore > 0.6) {
                action = "BUY";
                confidence = predictionScore;
            } else if (predictionScore < 0.4) {
                action = "SELL";
                confidence = 1.0f - predictionScore;
            } else {
                action = "HOLD";
                // Distance from midpoint
                confidence = Math.abs(predictionScore - 0.5) * 2;
            }

            return new Signal(symbol, action, confidence, System.currentTimeMillis());
        } catch (TranslateException e) {
            e.printStackTrace();
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }
    }

    @Override
    public String getName() {
        return "ML_PRE_TRAINED_STRATEGY";
    }

    private static class PriceSequenceTranslator implements Translator<double[], Float> {
        private final int windowSize;

        public PriceSequenceTranslator(int windowSize) {
            this.windowSize = windowSize;
        }

        @Override
        public NDList processInput(TranslatorContext ctx, double[] input) {
            NDManager manager = ctx.getNDManager();
            // Reshape into [1, windowSize] tensor depending on what your PyTorch model expects
            NDArray array = manager.create(input).reshape(1, windowSize);
            return new NDList(array);
        }

        @Override
        public Float processOutput(TranslatorContext ctx, NDList list) {
            // Assume the PyTorch model outputs a single float tensor
            return list.singletonOrThrow().getFloat();
        }

        @Override
        public Batchifier getBatchifier() {
            return null; // Batching is handled manually by reshaping
        }
    }
}
