package marketSystem.p;

import java.util.*;

class OutOfStockException extends Exception {
    OutOfStockException(String msg) { super(msg); }
}

class ProductNotFoundException extends Exception {
    ProductNotFoundException(String msg) { super(msg); }
}

class ExpenseException extends IllegalArgumentException {
    ExpenseException(String msg) { super(msg); }
}

class Product {
    int id;
    String name;
    double price;
    int stock;
    int soldCount = 0;

    Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    boolean reduceStock(int qty) throws OutOfStockException {
        if(qty > stock) throw new OutOfStockException("Not enough stock for " + name);
        stock -= qty;
        soldCount += qty;
        return true;
    }
}

class Sale {
    Product product;
    int quantity;
    double amount;
    String timeSlot;
    int loyaltyPoints;

    Sale(Product product, int quantity, String timeSlot) {
        this.product = product;
        this.quantity = quantity;
        double baseAmount = product.price * quantity;
        if(quantity >= 5) baseAmount *= 0.9;
        this.amount = baseAmount;
        this.timeSlot = timeSlot;
        this.loyaltyPoints = (int)(amount / 100);
    }
}

class Expense {
    String type;
    double cost;

    Expense(String type, double cost) {
        if(cost < 0) throw new ExpenseException("Expense cannot be negative!");
        this.type = type;
        this.cost = cost;
    }
}

class Market {
    Product[] products;
    Sale[] sales;
    Expense[] expenses;
    int productCount = 0;
    int saleCount = 0;
    int expenseCount = 0;
    int customersServed = 0;

    Market(int maxProducts, int maxSales, int maxExpenses) {
        products = new Product[maxProducts];
        sales = new Sale[maxSales];
        expenses = new Expense[maxExpenses];
    }

    void addProduct(Product p) {
        products[productCount++] = p;
    }

    void addExpense(String type, double cost) {
        expenses[expenseCount++] = new Expense(type, cost);
        System.out.println("Expense added: " + type + " → " + cost);
    }

    void recordSale(int productId, int qty, String timeSlot) throws ProductNotFoundException, OutOfStockException {
        for(int i = 0; i < productCount; i++) {
            if(products[i].id == productId) {
                products[i].reduceStock(qty);
                Sale sale = new Sale(products[i], qty, timeSlot);
                sales[saleCount++] = sale;
                customersServed++;
                System.out.println("Sale recorded! " + qty + " " + products[i].name + "(s) sold.");
                System.out.println("Loyalty Points earned: " + sale.loyaltyPoints);
                return;
            }
        }
        throw new ProductNotFoundException("Product ID " + productId + " not found!");
    }

    double totalIncome() {
        double sum = 0;
        for(int i = 0; i < saleCount; i++) sum += sales[i].amount;
        return sum;
    }

    double totalExpenses() {
        double sum = 0;
        for(int i = 0; i < expenseCount; i++) sum += expenses[i].cost;
        return sum;
    }

    double profit() {
        return totalIncome() - totalExpenses();
    }

    Product bestSeller() {
        if(productCount == 0) return null;
        Product best = products[0];
        for(int i = 1; i < productCount; i++) {
            if(products[i].soldCount > best.soldCount) best = products[i];
        }
        return best;
    }

    void dailySummary() {
        System.out.println("\n=== Daily Summary ===");
        System.out.println("Total Income: " + totalIncome());
        System.out.println("Total Expenses: " + totalExpenses());
        System.out.println("Profit: " + profit());
        System.out.println("Customers Served: " + customersServed);
        Product best = bestSeller();
        if(best != null) System.out.println("Best-Selling Product: " + best.name + " (" + best.soldCount + " sold)");
    }
}

class ReportThread extends Thread {
    Market market;

    ReportThread(Market market) { this.market = market; }

    public void run() {
        try {
            while(true) {
                System.out.println("\n[Auto Report] Income: " + market.totalIncome() +
                                   " | Profit: " + market.profit());
                Thread.sleep(15000);
            }
        } catch(InterruptedException e) {
            System.out.println("Report stopped.");
        }
    }
}

public class MarketSystem {
    public static void main(String[] args) {
        Market market = new Market(10, 50, 20);

        market.addProduct(new Product(1, "Milk", 50, 10));
        market.addProduct(new Product(2, "Soap", 30, 20));
        market.addProduct(new Product(3, "Bread", 40, 15));
        market.addProduct(new Product(4, "Rice", 60, 25));
        market.addProduct(new Product(5, "Eggs", 10, 50));

        System.out.println("=== Product List ===");
        for(int i = 0; i < market.productCount; i++) {
            Product p = market.products[i];
            System.out.println("ID: " + p.id + " | " + p.name + " | Price: " + p.price + " | Stock: " + p.stock);
        }

        ReportThread reportThread = new ReportThread(market);
        reportThread.setDaemon(true);
        reportThread.start();

        Scanner sc = new Scanner(System.in);

        while(true) {
            System.out.println("\n1. Record Sale\n2. Add Expense\n3. Show Total Income\n4. Exit");
            int choice = sc.nextInt();

            try {
                if(choice == 1) {
                    System.out.print("Product ID: ");
                    int id = sc.nextInt();
                    System.out.print("Quantity: ");
                    int qty = sc.nextInt();
                    System.out.print("Time Slot (Morning/Afternoon/Evening): ");
                    String slot = sc.next();
                    market.recordSale(id, qty, slot);
                }
                else if(choice == 2) {
                    System.out.print("Expense Type: ");
                    String type = sc.next();
                    System.out.print("Cost: ");
                    double cost = sc.nextDouble();
                    market.addExpense(type, cost);
                }
                else if(choice == 3) {
                    System.out.println("Total Income: " + market.totalIncome());
                }
                else if(choice == 4) {
                    market.dailySummary();
                    System.out.println("Exiting...");
                    break;
                }
            } catch(Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        sc.close();
    }
}
