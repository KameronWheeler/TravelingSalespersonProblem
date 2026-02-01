import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        ArrayList<City> cities = new ArrayList<>();
        String path = "src/hw1coord.txt"; //works if the txt file is stored in the same directory
        int generationNumber = 0;
        try {

            String line;
            int cityNumber = 0;
            BufferedReader br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                cityNumber++;
                cities.add(new City(cityNumber, Integer.parseInt(line.substring(0, line.indexOf(","))),
                        Integer.parseInt(line.substring(line.indexOf(",") + 1))));//I don't like how this section of code looks
                                                                                            //Can't explain it nor will I elaborate further
                if (cityNumber >= 10) {
                    break;
                }
            }

            double[][] adjacencyMatrix = generateAdjacencyMatrix(cities);

            for (int i = 0; i < cities.size(); i++) {
                for(int j = 0; j < cities.size(); j++) {
                    System.out.printf("%.4f",adjacencyMatrix[i][j]);//I always forget about printf()
                    System.out.print(" ");
                }
                System.out.println();
            }

            Generation gen = new Generation(generationNumber, cities, adjacencyMatrix);//initialize a new generation
            generationNumber++;//increment generation counter




        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    static double[][] generateAdjacencyMatrix(ArrayList<City> cities) {
        double[][] adjacencyMatrix = new double[cities.size()][cities.size()];//unpopulated array

        for (int i = 0; i < cities.size(); i++) {
            for (int j = 0; j < cities.size(); j++) {
                if (i == j) {//two cities cannot be the same
                    adjacencyMatrix[i][j] = 0; //populate diagonal with 0's
                    continue;//break from current iteration and increment counter
                }
                if (adjacencyMatrix[i][j] == 0) {//matrix will always be symmetric
                    double distance = calculateDistance(cities.get(i), cities.get(j));//calculate once and use for both positions
                    DecimalFormat df = new DecimalFormat("#.####");//we'll round off to 4 decimal places
                    distance = Double.parseDouble(df.format(distance));
                    adjacencyMatrix[i][j] = distance;
                    adjacencyMatrix[j][i] = distance;
                }
            }
        }
        return adjacencyMatrix;
    }

    static double calculateDistance(City city1, City city2) {
        return Math.sqrt(Math.pow(city1.getxCoordinate() - city2.getxCoordinate(), 2)
                + Math.pow(city1.getyCoordinate() - city2.getyCoordinate(), 2));
    }

    static class City {
        char name; //alias each city with a unique name (a-z for simplicity)
        int xCoordinate;
        int yCoordinate;

        City(int cityNumber, int xCoordinate, int yCoordinate) {
            name = (char)(cityNumber + 48);//assign names as ASCII values for easier addressing
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
        }

        @Override
        public String toString() {
            return name + " " + xCoordinate + " " + yCoordinate;
        }

        public char getName() {
            return name;
        }

        public int getxCoordinate() {
            return xCoordinate;
        }

        public int getyCoordinate() {
            return yCoordinate;
        }
    }

    static class Generation {
        public double fitness;//sum of all fitness for the generation
        public int generationNumber;//identifier for generation
        private final int populationSize = 10;
        private ArrayList<String> population = new ArrayList<>(); //save all members of the population
        private ArrayList<Double> routeFitnesses = new ArrayList<>();
        public Generation(int generationNumber, ArrayList<City> cities, double[][] adjacencyMatrix){

            generatePopulation(cities);
            sumFitnesses(adjacencyMatrix);

            calculateNormalizedFitness();//normalize
            System.out.println(routeFitnesses.toString());
            printPopulation();
            gnomeSort();
            printPopulation();
            System.out.println(routeFitnesses.toString());//print normalized fitnesses
        }

        public Generation(int generationNumber, ArrayList<City> cities, double[][] adjacencyMatrix, ArrayList<String> population){//overload operator
            this.population = population;//store children generated from the previous population
            this.generationNumber = generationNumber;
            fitness = 0;
            sumFitnesses(adjacencyMatrix);
            calculateNormalizedFitness();//normalize
            System.out.println(routeFitnesses.toString());
        }

        public String getPopulationAtIndex(int index){ //return route at a selected index
            return population.get(index);
        }

        public void gnomeSort(){//using gnome sort because of the low input size and negligible effect on performance
            int i = 1;
            while (i < populationSize) {
                if (i == 0 || routeFitnesses.get(i-1) <= routeFitnesses.get(i)) {
                    i++;
                } else {
                    swapFitness(routeFitnesses, i, i - 1);
                    swapPopulation(population, i, i - 1);
                    i--;
                }
            }
        }

        public void swapFitness(ArrayList<Double> values, int i, int j) {//swaps the fitness associated with a route
            double temp = values.get(i);
            values.set(i,values.get(j));
            values.set(j, temp);
        }

        public void swapPopulation(ArrayList<String> values, int i, int j) {//swaps population members
            String temp = values.get(i);
            values.set(i,values.get(j));
            values.set(j, temp);
        }

        private void sumFitnesses(double[][] adjacencyMatrix){

            for (String route : population) {
                double currentFitness = calculateFitness(route, adjacencyMatrix);
                routeFitnesses.add(currentFitness);
                fitness += currentFitness;
            }

        }

        private void calculateNormalizedFitness(){
            //I don't think that I need to use the individual fitness values after calculating this
            for (int i = 0; i < routeFitnesses.size(); i++) {
                routeFitnesses.set(i, routeFitnesses.get(i)/fitness);//replace each individual fitness with normalized fitness
            }
        }

        private void generatePopulation(ArrayList<City> cities) {//generate population of unique permutations
            String routeTaken = generateCityPermutation(cities);//initial permutation
            while(population.size() < populationSize){//generate 10 members
                while(population.contains(routeTaken)){
                    routeTaken = generateCityPermutation(cities);
                }
                population.add(routeTaken);
            }
        }

        public String generateCityPermutation(ArrayList<City> cities) {
            //this will only be used in the first generations, the following generations will be products of mutations and crossovers
            Random randNumber = new Random();
            StringBuilder sb = new StringBuilder();
            ArrayList<City> visitedCities = new ArrayList<>();//a list of cities visited in the current permutation

            for (int i = 0; i < cities.size(); i++) {
                int nextCity = randNumber.nextInt(cities.size());//generate random permutations for initial population
                while (visitedCities.contains(cities.get(nextCity))) {//if the city has already been visited in this permutation,
                    nextCity = randNumber.nextInt(cities.size());   //generate another city and try again
                }

                if (visitedCities.size() - 1 <= cities.size()) {
                    sb.append(cities.get(nextCity).getName());//append the name of the next city if it has not already been used
                    visitedCities.add(cities.get(nextCity));//add city to visitedCities list
                }
            }
            return sb.toString();//return the built permutation
        }

        public static double calculateFitness(String routeTaken, double[][] adjacencyMatrix) {
            double distance = 0;
            int city1;//point a
            int city2;//point b
            int i;
            for (i = 0; i < routeTaken.length()-2; i++) {
                city1 = Integer.parseInt(routeTaken.substring(i, i + 1));//get first city
                city2 = Integer.parseInt(routeTaken.substring(i + 1, i + 2));//get second city
                distance += adjacencyMatrix[city1-1][city2-1];//add distance from city 1 to city 2
            }
            if (i == routeTaken.length() - 1) { //wraparound from last city to first city
                city1 = Integer.parseInt(routeTaken.substring(routeTaken.length() - 1));//get last city
                city2 = Integer.parseInt(routeTaken.substring(0, 1));//get original city
                distance += adjacencyMatrix[city1][city2];//add distance from city 1 to city 2

            }
            return 1/distance;
        }

        public void printPopulation(){
            for (String route : population){
                System.out.print(route + " ");
            }
            System.out.println();
        }

    }
}