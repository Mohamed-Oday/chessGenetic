package org.openjfx.chessgenetic;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.*;

/**
 * This class is responsible for initializing the chess board and starting the genetic algorithm.
 * It also handles the animation of the genetic algorithm and updating the chess board with the current solution.
 */

public class ChessBoardController {
    @FXML private GridPane chessBoard;          // The chess board grid
    @FXML private TextField populationSize;     // The population size
    @FXML private TextField targetFitness;      // The target fitness
    @FXML private TextField maxGenerations;     // The maximum number of generations
    @FXML private TextField conflictsDisplay;   // The number of conflicts

    private List<ChessMatrix> population;       // The current population
    private Timeline animation;                 // The animation timeline
    private int currentIndex;                   // The current index for animation
    private ChessMatrix ultimateSolution;       // The ultimate solution found
    private int generationCount;                // The current generation count

    /**
     * Initializes the chess board grid with alternating light and dark squares.
     */
    @FXML
    public void initialize() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = new StackPane();
                String color = (row + col) % 2 == 0 ? "light-square" : "dark-square";
                square.getStyleClass().addAll("chess-square", color);
                chessBoard.add(square, col + 1, row); // +1 for coordinate labels
            }
        }

        if (conflictsDisplay != null) {
            conflictsDisplay.setEditable(false);
            conflictsDisplay.setText("0");
        }
    }

    /*
    @FXML
    private void startEvolution() {
        // Stop any existing animation
        if (animation != null) {
            animation.stop();
        }

        // Parse parameters
        int popSize = Integer.parseInt(populationSize.getText());
        int maxGen = Integer.parseInt(maxGenerations.getText());
        double targetFitness = Double.parseDouble(this.targetFitness.getText());
        conflictsDisplay.clear();

        // Reset generation counter and ultimate solution
        generationCount = 0;
        ultimateSolution = new ChessMatrix(); // Initialize with worst possible solution

        // Generate initial population
        population = new ArrayList<>(popSize);
        for (int i = 0; i < popSize; i++) {
            population.add(new ChessMatrix());
        }

        // Reset index for animation
        currentIndex = 0;

        // Create animation timeline
        animation = new Timeline(
                new KeyFrame(Duration.seconds(0.03), e -> {
                    if (currentIndex < population.size()) {
                        // Display current solution during animation
                        ChessMatrix current = population.get(currentIndex);
                        displaySolution(current);
                        conflictsDisplay.setText(String.valueOf(current.getConflicts()));
                        currentIndex++;
                    } else {
                        // Evolution logic
                        evolvePopulation(popSize, maxGen);
                    }
                })
        );

        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }*/

    /*private void evolvePopulation(int popSize, int maxGen) {
        // Find and display best solution in the current generation
        ChessMatrix bestSolution = findBestSolution();
        displaySolution(bestSolution);
        conflictsDisplay.setText(String.valueOf(bestSolution.getConflicts()));

        // Update ultimate solution if the current best is better
        if (bestSolution.getConflicts() < ultimateSolution.getConflicts()) {
            System.out.println("New ultimate solution found: " + bestSolution.getConflicts());
            ultimateSolution = bestSolution;
        }

        System.out.println("Generation: " + generationCount + ", Best solution: " + bestSolution.getConflicts());

        // Increment generation
        generationCount++;

        // Check termination conditions
        if (ultimateSolution.getConflicts() == 0 || generationCount >= maxGen) {
            System.out.println("Evolution complete after " + generationCount + " generations");
            System.out.println("Ultimate solution has " + ultimateSolution.getConflicts() + " conflicts");

            // Ensure we display the ultimate solution
            displaySolution(ultimateSolution);
            conflictsDisplay.setText(String.valueOf(ultimateSolution.getConflicts()));

            animation.stop();
            return;
        }

        // Create next generation
        System.out.println("Starting Crossover Generation: " + generationCount + ", Best solution: " + bestSolution.getConflicts());
        List<Pair<ChessMatrix, ChessMatrix>> parentPairs = selectParents(population, popSize / 2);
        population = crossOver(parentPairs);

        // Reset for next animation cycle
        currentIndex = 0;
    }*/

    /**
     *  Starts the genetic algorithm.
     *  Steps:
     *  1- Parse parameters (gets the params from the user (populationSize, maxGeneration, targetFitness))
     *  2- Initialize generationCount and ultimateSolution
     *  3- Generate pop size initial population
     *  4- Find initial best solution
     *  5- Run evolution loop until we reach either the max gen or conflict = 0
     *      5.1 Find the best solution in current generation
     *      5.2 Update ultimate solution if the current best solution is better than the ultimate solution
     *      5.3 Selection step: Select parents for crossover
     *      5.4 Crossover step: Create offspring from parent pairs
     *      5.5 Mutation step: Mutate offspring (in our case it happens only to crossover offspring)
     * @see <a href="https://en.wikipedia.org/wiki/Genetic_algorithm">Genetic Algorithm</a>
     */
    @FXML
    private void startEvolution() {
        // Stop any existing animation
        if (animation != null) {
            animation.stop();
        }

        // 1- Parse parameters (gets the params from the user (populationSize, maxGeneration, targetFitness))
        int popSize = Integer.parseInt(populationSize.getText());
        int maxGen = Integer.parseInt(maxGenerations.getText());
        double targetFitness = Double.parseDouble(this.targetFitness.getText());

        conflictsDisplay.clear();

        // 2- Initialize generationCount and ultimateSolution
        generationCount = 0;
        ultimateSolution = new ChessMatrix();

        // 3- Generate pop size initial population
        population = new ArrayList<>(popSize);
        for (int i = 0; i < popSize; i++) {
            population.add(new ChessMatrix());
        }

        // Console display (Debugging)
        System.out.println("Starting fast evolution with population size: " + popSize + ", max generations: " + maxGen);

        // 4- Find initial best solution
        ChessMatrix bestSolution = findBestSolution();
        ultimateSolution = bestSolution;

        // 5- Run evolution loop until we reach either the max gen or conflict = 0
        while (generationCount < maxGen && ultimateSolution.getConflicts() > 0) {
            // 5.1 Find the best solution in current generation
            bestSolution = findBestSolution();

            // 5.2 Update ultimate solution if the current best solution is better than the ultimate solution
            if (bestSolution.getConflicts() < ultimateSolution.getConflicts()) {
                System.out.println("New ultimate solution found: " + bestSolution.getConflicts()); // Console display (Debugging)
                ultimateSolution = bestSolution;
            }

            // Console display (Debugging)
            System.out.println("Generation: " + generationCount + ", Best solution: " + bestSolution.getConflicts());

            // 5.3 Selection step: Select parents for crossover
            List<Pair<ChessMatrix, ChessMatrix>> parentPairs = selectParents(population, popSize / 2);

            // 5.4 Crossover step: Create offspring from parent pairs
            // 5.5 Mutation step: Mutate offspring (in our case it happens only to crossover offspring)
            population = crossOver(parentPairs);

            // Increment generation count
            generationCount++;
        }

        // Evolution complete - display final solution
        System.out.println("Evolution complete after " + generationCount + " generations");
        System.out.println("Ultimate solution has " + ultimateSolution.getConflicts() + " conflicts");

        // Display the ultimate solution in the UI
        displaySolution(ultimateSolution);
        conflictsDisplay.setText(String.valueOf(ultimateSolution.getConflicts()));
    }


    /**
     * Finds the best solution in the current population.
     * @return The ChessMatrix with the lowest number of conflicts
     */
    private ChessMatrix findBestSolution() {
        return population.stream()
                .min(Comparator.comparingInt(ChessMatrix::getConflicts))
                .orElseThrow(() -> new RuntimeException("Population is empty"));
    }


    /**
     * Crossover step: Create offspring from parent pairs
     * the mutation happens here
     * @see ChessMatrix
     * @param parentPairs
     * @return offSpring List
     */
    private List<ChessMatrix> crossOver(List<Pair<ChessMatrix, ChessMatrix>> parentPairs) {

        // Console display (Debugging)
        System.out.println("Starting Crossover");

        // Initialize an offSpring list
        List<ChessMatrix> offSpring = new ArrayList<>();

        // Loop through the parent pairs
        for (Pair<ChessMatrix, ChessMatrix> pair: parentPairs) {
            Vector<Character> parent1 = pair.getFirst().getBoardVictor();
            Vector<Character> parent2 = pair.getSecond().getBoardVictor();

            // Create children
            ChessMatrix child1 = new ChessMatrix();
            ChessMatrix child2 = new ChessMatrix();

            // Get random value for the crossover probability = 80%
            if (Math.random() <= 0.8) {

                // Single point crossover
                int crossoverPoint = 32;
                for (int i = 0; i < 64; i++) {
                    if (i < crossoverPoint) {
                        child1.getBoardVictor().add(parent1.get(i));
                        child2.getBoardVictor().add(parent2.get(i));
                    } else {
                        child1.getBoardVictor().add(parent2.get(i));
                        child2.getBoardVictor().add(parent1.get(i));
                    }
                }
                // Mutate children
                child1.mutate();
                child2.mutate();

                // Add children to the offspring list
                offSpring.add(child1);
                offSpring.add(child2);
            }else {
                // If the random value is greater than 0.8, add the parents to the offspring list
                offSpring.add(pair.getFirst());
                offSpring.add(pair.getSecond());
            }
        }

        return offSpring;
    }

    /**
     * Selects parent pairs using roulette wheel selection based on cumulative probability.
     * Each parent is selected only once, and probabilities are recalculated after each selection.
     * @param population The list of ChessMatrix individuals
     * @param numPairs The number of parent pairs to select
     * @return List of parent pairs for crossover
     */
    private List<Pair<ChessMatrix, ChessMatrix>> selectParents(List<ChessMatrix> population, int numPairs) {
        // Create a copy of the population to work with
        List<ChessMatrix> remainingPopulation = new ArrayList<>(population);
        List<Pair<ChessMatrix, ChessMatrix>> parentPairs = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numPairs; i++) {
            // Recalculate selection fitness for the remaining population
            calculateSelectionFitness(remainingPopulation);

            // Calculate cumulative probabilities
            List<Double> cumulativeProbabilities = new ArrayList<>();
            double sum = 0;
            for (ChessMatrix individual : remainingPopulation) {
                sum += individual.getSelectionFitness();
                cumulativeProbabilities.add(sum);
            }

            // Ensure we have enough individuals to form a pair
            if (remainingPopulation.size() < 2) {
                break;
            }

            // Select first parent
            double r1 = random.nextDouble();
            int firstIndex = selectIndividualIndex(cumulativeProbabilities, r1);
            ChessMatrix parent1 = remainingPopulation.get(firstIndex);

            // Remove the first parent from the remaining population
            remainingPopulation.remove(firstIndex);

            // Recalculate selection fitness and cumulative probabilities
            calculateSelectionFitness(remainingPopulation);
            cumulativeProbabilities.clear();
            sum = 0;
            for (ChessMatrix individual : remainingPopulation) {
                sum += individual.getSelectionFitness();
                cumulativeProbabilities.add(sum);
            }

            // Select second parent
            double r2 = random.nextDouble();
            int secondIndex = selectIndividualIndex(cumulativeProbabilities, r2);
            ChessMatrix parent2 = remainingPopulation.get(secondIndex);

            // Remove the second parent from the remaining population
            remainingPopulation.remove(secondIndex);

            // Create pair and add to list
            parentPairs.add(new Pair<>(parent1, parent2));
        }

        return parentPairs;
    }

    /**
     * Calculate the selection fitness for each individual in the population.
     * @param population
     */
    private void calculateSelectionFitness(@NotNull List<ChessMatrix> population) {
        // Calculate the total fitness
        double totalFitness = population.stream()
                .mapToDouble(ChessMatrix::getFitness)
                .sum();

        for (ChessMatrix matrix : population) {
            matrix.setSelectionFitness(matrix.getFitness() / totalFitness);
        }
    }

    /**
     * Selects the index of an individual based on the cumulative probabilities.
     * @param cumulativeProbabilities The list of cumulative probabilities
     * @param randomValue The random value to use for selection
     * @return The index of the selected individual
     */
    private int selectIndividualIndex(List<Double> cumulativeProbabilities, double randomValue) {
        for (int j = 0; j < cumulativeProbabilities.size(); j++) {
            if (randomValue <= cumulativeProbabilities.get(j)) {
                return j;
            }
        }
        // Fallback to last individual if no match found (shouldn't happen if probabilities sum to 1)
        return cumulativeProbabilities.size() - 1;
    }

    /**
     * Simple Pair inner class to hold two ChessMatrix objects
     */
    private static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }

    /**
     * Ui display method
     * @param solution
     */
    private void displaySolution(ChessMatrix solution) {
        // Clear current board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = (StackPane) chessBoard.getChildren().get(row * 8 + col);
                square.getChildren().clear();

                char piece = solution.getBoard()[row][col];
                if (piece != 0) {
                    try {
                        ImageView pieceImage = new ImageView();

                        String imagePath = switch (piece) {
                            case 'Q' -> "/org/openjfx/chessgenetic/whiteQ.png";
                            case 'q' -> "/org/openjfx/chessgenetic/blackQ.png";
                            case 'R' -> "/org/openjfx/chessgenetic/whiteR.png";
                            case 'r' -> "/org/openjfx/chessgenetic/blackR.png";
                            case 'K' -> "/org/openjfx/chessgenetic/whiteK.png";
                            case 'k' -> "/org/openjfx/chessgenetic/blackK.png";
                            case 'B' -> "/org/openjfx/chessgenetic/whiteB.png";
                            case 'b' -> "/org/openjfx/chessgenetic/blackB.png";
                            default -> null;
                        };

                        if (imagePath != null) {
                            // Use safe resource loading
                            var imageStream = getClass().getResourceAsStream(imagePath);
                            if (imageStream != null) {
                                pieceImage.setImage(new javafx.scene.image.Image(imageStream));
                                pieceImage.setFitHeight(48);
                                pieceImage.setFitWidth(48);
                                pieceImage.setPreserveRatio(true);
                                square.getChildren().add(pieceImage);
                            } else {
                                // Fallback to text representation
                                Label pieceLabel = new Label(String.valueOf(piece));
                                pieceLabel.getStyleClass().add("chess-piece");
                                square.getChildren().add(pieceLabel);
                                System.out.println("Could not find image: " + imagePath);
                            }
                        } else {
                            // For cases where no image path is defined
                            Label pieceLabel = new Label(String.valueOf(piece));
                            pieceLabel.getStyleClass().add("chess-piece");
                            square.getChildren().add(pieceLabel);
                        }
                    } catch (Exception e) {
                        // Provide detailed error information
                        System.err.println("Error loading image for piece: " + piece);
                        e.printStackTrace();

                        // Fallback to text representation
                        Label pieceLabel = new Label(String.valueOf(piece));
                        pieceLabel.getStyleClass().add("chess-piece");
                        square.getChildren().add(pieceLabel);
                    }
                }
            }
        }
    }
}