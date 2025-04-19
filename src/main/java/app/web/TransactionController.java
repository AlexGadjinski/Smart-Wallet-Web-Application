package app.web;

import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping()
    public ModelAndView getAllTransactions() {
        List<Transaction> transactions =
                transactionService.getAllByOwnerId(UUID.fromString("e637f917-e02d-4a8d-9682-6da71dc69b12"));
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("transactions");
        modelAndView.addObject("transactions", transactions);

        return modelAndView;
    }
}
