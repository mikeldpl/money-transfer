package com.github.mikeldpl.hw.money.transfer;

import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.exception.ApiException;
import com.github.mikeldpl.hw.money.transfer.exception.NotFoundApiException;
import com.github.mikeldpl.hw.money.transfer.exception.ValidationApiException;
import com.github.mikeldpl.hw.money.transfer.model.Account;
import com.github.mikeldpl.hw.money.transfer.model.ErrorMessage;
import com.github.mikeldpl.hw.money.transfer.model.Transfer;
import com.github.mikeldpl.hw.money.transfer.model.TransferAction;
import com.github.mikeldpl.hw.money.transfer.service.AccountService;
import com.github.mikeldpl.hw.money.transfer.service.DbUpdateService;
import com.github.mikeldpl.hw.money.transfer.service.OldTransfersHandlerService;
import com.github.mikeldpl.hw.money.transfer.service.TransferActionService;
import com.github.mikeldpl.hw.money.transfer.service.TransferService;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

@Singleton
public class MoneyTransferRestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneyTransferRestServer.class);

    private static final String CONTENT_TYPE = "application/json";
    private final DbUpdateService dbUpdateService;
    private final AccountService accountService;
    private final TransferService transferService;
    private final TransferActionService transferActionService;
    private final OldTransfersHandlerService oldTransfersHandlerService;
    private final Gson gson;

    @Inject
    public MoneyTransferRestServer(DbUpdateService dbUpdateService, AccountService accountService,
                                   TransferService transferService,
                                   TransferActionService transferActionService,
                                   OldTransfersHandlerService oldTransfersHandlerService, Gson gson) {
        this.dbUpdateService = dbUpdateService;
        this.accountService = accountService;
        this.transferService = transferService;
        this.transferActionService = transferActionService;
        this.oldTransfersHandlerService = oldTransfersHandlerService;
        this.gson = gson;
    }

    void run() {
        dbUpdateService.updateDbTables();
        oldTransfersHandlerService.startScheduler();

        configureRouting();
        configureErrorHandling();

        Spark.awaitInitialization();
    }

    private void configureErrorHandling() {
        ErrorMessage error500 = new ErrorMessage("Unexpected server error.");
        ErrorMessage error404 = new ErrorMessage("Not found.");

        Spark.internalServerError((request, response) -> provideResponse(response, 500, error500));
        Spark.notFound((request, response) -> provideResponse(response, 404, error404));
        Spark.exception(ApiException.class, (exception, request, response) -> {
            LOGGER.error("Unexpected api exception", exception);
            ErrorMessage errorMessage = new ErrorMessage(exception.getMessage());
            provideResponse(response, 500, errorMessage);
        });
        Spark.exception(NotFoundApiException.class, (exception, request, response) -> {
            String message = "Not found exception: " + exception.getMessage();
            ErrorMessage errorMessage = new ErrorMessage(message);
            provideResponse(response, 404, errorMessage);
        });
        Spark.exception(ValidationApiException.class, (exception, request, response) -> {
            String message = "Validation exception: " + exception.getMessage();
            ErrorMessage errorMessage = new ErrorMessage(message);
            provideResponse(response, 400, errorMessage);
        });
        Spark.exception(JsonParseException.class, (exception, request, response) -> {
            String message = "JSON parsing exception: " + exception.getMessage();
            ErrorMessage errorMessage = new ErrorMessage(message);
            provideResponse(response, 400, errorMessage);
        });
    }

    private void configureRouting() {
        path("/money-transfer/1", () -> {
            path("/accounts", () -> {
                get("", (request, response) -> {
                    List<Account> all = accountService.getAll();
                    return provideResponse(response, 200, all);
                });
                post("", (request, response) -> {
                    Account account = extractBody(request, Account.class);
                    Account createdAccount = accountService.createAccount(account);
                    return provideResponse(response, 201, createdAccount);
                });
                get("/:id", (request, response) -> {
                    Long id = getLongParam(request, "id");
                    Account account = accountService.getAccount(id);
                    return provideResponse(response, 200, account);
                });
            });

            path("/accounts/:account_id/transfers", () -> {
                get("", (request, response) -> {
                    Long accountId = getLongParam(request, "account_id");
                    List<Transfer> allBiAccount = transferService.getAllByAccountId(accountId);
                    return provideResponse(response, 200, allBiAccount);
                });
                post("", (request, response) -> {
                    Long accountId = getLongParam(request, "account_id");
                    Transfer transfer = extractBody(request, Transfer.class);
                    Transfer created = transferService.createTransfer(accountId, transfer);
                    return provideResponse(response, 201, created);
                });
                get("/:id", (request, response) -> {
                    Long accountId = getLongParam(request, "account_id");
                    Long id = getLongParam(request, "id");
                    Transfer transfer = transferService.getByAccountIdAndId(accountId, id);
                    return provideResponse(response, 200, transfer);
                });


            });

            path("/accounts/:account_id/transfers/:transfer_id/actions", () -> {
                get("", (request, response) -> {
                    Long accountId = getLongParam(request, "account_id");
                    Long transferId = getLongParam(request, "transfer_id");
                    List<TransferAction> allBiAccount = transferActionService.getAllByTransferId(accountId, transferId);
                    return provideResponse(response, 200, allBiAccount);
                });
                post("", (request, response) -> {
                    Long accountId = getLongParam(request, "account_id");
                    Long transferId = getLongParam(request, "transfer_id");
                    TransferAction transferAction = extractBody(request, TransferAction.class);
                    TransferAction created = transferActionService.createTransferAction(accountId, transferId, transferAction);
                    return provideResponse(response, 201, created);
                });
                get("/:id", (request, response) -> {
                    Long accountId = getLongParam(request, "account_id");
                    Long transferId = getLongParam(request, "transfer_id");
                    Long id = getLongParam(request, "id");
                    TransferAction transferAction = transferActionService.getByTransferIdAndId(accountId, transferId, id);
                    return provideResponse(response, 200, transferAction);
                });
            });
        });
    }

    private Long getLongParam(Request request, String name) {
        String value = request.params(name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ValidationApiException("Path parameter '" + name + "' has not numeric value: '" + value + "'.");
        }
    }


    private <T> T extractBody(Request request, Class<T> classOfT) {
        T body = gson.fromJson(request.body(), classOfT);
        if (body == null) {
            throw new ValidationApiException("Request payload is required.");
        }
        return body;
    }

    private Object provideResponse(Response response, int status, Object body) {
        response.status(status);
        response.type(CONTENT_TYPE);
        String bodyJson = gson.toJson(body);
        response.body(bodyJson);
        return bodyJson;
    }
}
