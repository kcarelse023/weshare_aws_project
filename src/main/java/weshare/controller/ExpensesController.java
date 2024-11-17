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

    public static final Handler view = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);

        Collection<Expense> unpaidExpenses = expenses.stream()
                .filter(expense -> !expense.isFullyPaidByOthers())
                .toList();

        MonetaryAmount grandTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(ZERO_RANDS, MonetaryAmount::add);

        Map<String, Object> viewModel = Map.of(
                "expenses", unpaidExpenses,
                "grandTotal", grandTotal
        );

        context.render("expenses.html", viewModel);
    };

    public static final Handler newExpenses = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        context.sessionAttribute(WeShareServer.SESSION_USER_KEY, personLoggedIn);
        context.render("newexpense.html");
    };


    public static final Handler addExpenses = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String description = context.formParam("description");
        long amount = Long.parseLong(context.formParam("amount"));
        LocalDate date = LocalDate.parse(context.formParam("date"), DateHelper.DD_MM_YYYY);

        MonetaryAmount monetaryAmount = MoneyHelper.amountOf(amount);
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
