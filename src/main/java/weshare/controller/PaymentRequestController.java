package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.*;
import weshare.persistence.ExpenseDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.*;

import static weshare.model.MoneyHelper.ZERO_RANDS;

public class PaymentRequestController {

    /**
     * Displays a specific expense and its associated payment requests.
     * The logged-in user can view the details of an expense and payment requests they've sent.
     */
    public static final Handler viewPaymentRequest = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        String expenseId = context.queryParam("expenseId");

        Optional<Expense> optionalExpense = expensesDAO.get(UUID.fromString(expenseId));
        Expense expense = optionalExpense.get();
        Collection<PaymentRequest> requests = expensesDAO.findPaymentRequestsSent(personLoggedIn);

        Map<String, Object> viewModel = Map.of(
                "expense", expense,
                "requests", requests
        );

        context.render("paymentrequest.html", viewModel);
    };

    /**
     * Displays all payment requests sent by the logged-in user.
     * Calculates the total amount of pending payments and renders the "sent" view.
     */
    public static final Handler viewSent = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Collection<PaymentRequest> requests = expensesDAO.findPaymentRequestsSent(personLoggedIn);
        MonetaryAmount grandTotal = requests.stream()
                .map(PaymentRequest::getAmountToPay)
                .reduce(ZERO_RANDS, MonetaryAmount::add);

        Map<String, Object> viewModel = Map.of(
                "requests", requests,
                "grandTotal", grandTotal
        );

        context.render("paymentrequest_sent.html", viewModel);
    };

    /**
     * Displays all payment requests received by the logged-in user.
     * Calculates the total amount they need to pay and renders the "received" view.
     */
    public static final Handler viewReceived = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Collection<PaymentRequest> paymentRequests = expensesDAO.findPaymentRequestsReceived(personLoggedIn);
        MonetaryAmount grandTotal = paymentRequests.stream()
                .map(PaymentRequest::getAmountToPay)
                .reduce(ZERO_RANDS, MonetaryAmount::add);

        Map<String, Object> viewModel = Map.of(
                "PaymentRequest", paymentRequests,
                "grandTotal", grandTotal
        );

        context.render("paymentrequest_received.html", viewModel);
    };

    /**
     * Handles the submission of a new payment request.
     * Captures the form data, creates a payment request, and updates the expense with this request.
     */
    public static Handler sendPaymentRequest = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String expenseId = context.formParam("expense_id");
        String email = context.formParam("email");
        String amount = context.formParam("amountRequested");
        LocalDate dueDate = LocalDate.parse(context.formParam("dueDate"), DateHelper.DD_MM_YYYY);

        Expense expense = expensesDAO.get(UUID.fromString(expenseId)).get();
        Person personWhoShouldPayBack = new Person(email);
        MonetaryAmount amountToPay = MoneyHelper.amountOf(Long.parseLong(amount));

        PaymentRequest paymentRequest = new PaymentRequest(expense, personWhoShouldPayBack, amountToPay, dueDate);
        expense.requestPayment(personWhoShouldPayBack, paymentRequest.getAmountToPay(), dueDate);

        expensesDAO.save(expense);

        context.redirect("/paymentrequest?expenseId=" + expenseId);
    };

    /**
     * Processes the payment of a specific payment request.
     * Updates the relevant expenses and redirects the user to the received payments page.
     */
    public static Handler payPaymentRequest = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        UUID expenseId = UUID.fromString(context.formParam("expenseId"));
        UUID paymentRequestId = UUID.fromString(context.formParam("paymentRequestId"));

        Expense expense1 = expensesDAO.get(expenseId).get();
        MonetaryAmount amountPaid = expense1.totalAmountOfPaymentsRequested();
        Expense expense2 = new Expense(personLoggedIn, expense1.getDescription(), amountPaid, expense1.getDate());

        expense1.payPaymentRequest(paymentRequestId, personLoggedIn, LocalDate.now());

        expensesDAO.save(expense1);
        expensesDAO.save(expense2);

        context.redirect(Routes.RECEIVED);
    };
}
