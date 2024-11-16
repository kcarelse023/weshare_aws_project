package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    // Login and logout routes
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";

    // Expense-related routes
    public static final String EXPENSES = "/expenses";
    public static final String NEWEXPENSE = "/newexpense";
    public static final String ADDEXPENSE = "/expense.action";

    // Payment request routes
    public static final String PAYMENT_REQUEST = "/paymentrequest";
    public static final String SENT = "/paymentrequests_sent";
    public static final String RECEIVED = "/paymentrequests_received";
    public static final String SUBMIT_PAYMENT_REQUEST = "/paymentrequest.action";
    public static final String PAY_PAYMENT_REQUEST = "/payment.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            // Login and logout routes
            post(LOGIN_ACTION, PersonController.login);
            get(LOGOUT, PersonController.logout);

            // Expense-related routes
            get(EXPENSES, ExpensesController.view);
            post(NEWEXPENSE, ExpensesController.newExpenses);
            post(ADDEXPENSE, ExpensesController.addExpenses);

            // Payment request routes
            get(PAYMENT_REQUEST, PaymentRequestController.viewPaymentRequest);
            get(SENT, PaymentRequestController.viewSent);
            get(RECEIVED, PaymentRequestController.viewReceived);
            post(SUBMIT_PAYMENT_REQUEST, PaymentRequestController.sendPaymentRequest);
            post(PAY_PAYMENT_REQUEST, PaymentRequestController.payPaymentRequest);
        });
    }
}
