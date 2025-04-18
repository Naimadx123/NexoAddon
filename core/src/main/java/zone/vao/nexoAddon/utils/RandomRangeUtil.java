package zone.vao.nexoAddon.utils;

import java.util.Random;

public class RandomRangeUtil {

    private static final Random random = new Random();

    public static double parseAndGetRandomValue(String input) {
        if (input.contains("..")) {
            String[] parts = input.split("\\.\\.");
            try {
                double num1 = parts[0].isEmpty() ? 0 : Double.parseDouble(parts[0]);
                double num2 = (parts.length < 2 || parts[1].isEmpty()) ? num1 + 1 : Double.parseDouble(parts[1]);
                double min = Math.min(num1, num2);
                double max = Math.max(num1, num2);
                return min + (max - min) * random.nextDouble();
            } catch (NumberFormatException e) {
                return 0.5;
            }
        }
        else if (input.contains("-")) {
            String[] parts = input.split("-");
            if (parts.length == 2) {
                try {
                    double num1 = Double.parseDouble(parts[0]);
                    double num2 = Double.parseDouble(parts[1]);
                    double min = Math.min(num1, num2);
                    double max = Math.max(num1, num2);
                    return min + (max - min) * random.nextDouble();
                } catch (NumberFormatException e) {
                    return 0.5;
                }
            } else if (parts.length == 3 && parts[0].isEmpty()) {
                try {
                    double num1 = Double.parseDouble("-" + parts[1]);
                    double num2 = Double.parseDouble("-" + parts[2]);
                    double min = Math.min(num1, num2);
                    double max = Math.max(num1, num2);
                    return min + (max - min) * random.nextDouble();
                } catch (NumberFormatException e) {
                    return 0.5;
                }
            } else if (parts.length == 3) {
                try {
                    double num1 = Double.parseDouble(parts[0]);
                    double num2 = Double.parseDouble("-" + parts[2]);
                    double min = Math.min(num1, num2);
                    double max = Math.max(num1, num2);
                    return min + (max - min) * random.nextDouble();
                } catch (NumberFormatException e) {
                    return 0.5;
                }
            }
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0.5;
        }
    }
}