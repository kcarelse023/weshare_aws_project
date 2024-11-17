package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.DateHelper;
import weshare.model.Expense;
import weshare.model.MoneyHelper;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static weshare.model.MoneyHelper.ZERO_RANDS;

public class ExpensesController {

    /**
     * Handler to view the list of expenses.
     * It retrieves the logged-in user's expenses and calculates the total unpaid amount.
     * The resulting view model is passed to the "expenses.html" template for rendering.
     */
    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        // Retrieve all expenses for the logged-in person
        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);

        // Filter expenses to include only those that are not fully paid
        Collection<Expense> unpaidExpenses = expenses.stream()
                .filter(expense -> !expense.isFullyPaidByOthers())
                .toList();

        // Calculate the total amount of all expenses
        MonetaryAmount grandTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(ZERO_RANDS, MonetaryAmount::add);

        // Create the view model to pass data to the template
        Map<String, Object> viewModel = Map.of(
                "expenses", unpaidExpenses,
                "grandTotal", grandTotal
        );

        // Render the expenses page with the view model
        context.render("expenses.html", viewModel);
    };

    /**
     * Handler to render the form for creating a new expense.
     * This handler retrieves the logged-in user's session and displays the form.
     */
    public static final Handler newExpenses = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        context.sessionAttribute(WeShareServer.SESSION_USER_KEY, personLoggedIn);
        context.render("newexpense.html");
    };

    /**
     * Handler to add a new expense based on form submission.
     * The handler reads the form parameters, creates a new Expense object, and saves it to the database.
     * It then calculates the updated list of expenses and redirects the user to the expenses page.
     */
    public static final Handler addExpenses = context -> {
        String description = context.formParam("description");
        long amount = Long.parseLong(context.formParam("amount"));
        MonetaryAmount monetaryAmount = MoneyHelper.amountOf(amount);
        LocalDate date = LocalDate.parse(context.formParam("date"), DateHelper.DD_MM_YYYY);

        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Expense newExpense = new Expense(personLoggedIn, description, monetaryAmount, date);
        expensesDAO.save(newExpense);
        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        MonetaryAmount grandTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(ZERO_RANDS, MonetaryAmount::add);

        Map<String, Object> viewModel = Map.of(
                "expenses", expenses,
                "grandTotal", grandTotal
        );

        context.render("expenses.html", viewModel);
        context.redirect("/expenses");
    };
}
