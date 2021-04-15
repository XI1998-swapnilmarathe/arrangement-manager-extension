package com.backbase.dbs.product.summary;

import com.backbase.dbs.product.Configurations;
import com.backbase.dbs.product.arrangement.ArrangementService;
import com.backbase.dbs.product.balance.BalanceService;
import com.backbase.dbs.product.common.clients.AccessGroupClient;
import com.backbase.dbs.product.common.clients.UserClient;
import com.backbase.dbs.product.persistence.Arrangement;
import com.backbase.dbs.product.repository.ArrangementJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Primary
@Service
@Validated
public class ExtendedProductSummaryService extends ProductSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ExtendedProductSummaryService.class);
    private static final String MAURITIUS_RUPEE= "EUR";
    private BigDecimal THRESHOLD_BALANCE= new BigDecimal(50); //TODO decide how we will get this

    final Predicate<Arrangement> currencyCodePredicate = account -> !account.getCurrency().getCurrencyCode().equals(MAURITIUS_RUPEE);
    final Predicate<Arrangement> balancePredicate = account -> account.getAvailableBalance().compareTo(THRESHOLD_BALANCE) >= 0;

    public ExtendedProductSummaryService(Configurations configurations, ArrangementService arrangementService, UserClient userClient, AccessGroupClient accessGroupClient, ArrangementJpaRepository arrangementRepository, BalanceService balanceService) {
        super(configurations, arrangementService, userClient, accessGroupClient, arrangementRepository, balanceService);
    }

    @Override
    public ProductSummary getProductSummary(Boolean debitAccount, Boolean creditAccount, String locale) {
        //prehook
        log.info("Prehook for extended getProductSummary");
        final ProductSummary productSummary = super.getProductSummary(debitAccount, creditAccount, locale);
        log.info("Posthook for extended getProductSummary");

        final List<Arrangement> filteredCurrentAccounts = filterArrangements(productSummary.getCurrentAccounts());
        productSummary.getCurrentAccounts().clear();
        productSummary.getCurrentAccounts().addAll(filteredCurrentAccounts);

        final List<Arrangement> filteredCreditCards = filterArrangements(productSummary.getCreditCards());
        productSummary.getCreditCards().clear();
        productSummary.getCreditCards().addAll(filteredCreditCards);

        final List<Arrangement> filteredDebitCards = filterArrangements(productSummary.getDebitCards());
        productSummary.getDebitCards().clear();
        productSummary.getDebitCards().addAll(filteredDebitCards);

        final List<Arrangement> filteredInvestmentAccounts = filterArrangements(productSummary.getInvestmentAccounts());
        productSummary.getInvestmentAccounts().clear();
        productSummary.getInvestmentAccounts().addAll(filteredInvestmentAccounts);


        final List<Arrangement> filteredLoans = filterArrangements(productSummary.getLoans());
        productSummary.getLoans().clear();
        productSummary.getLoans().addAll(filteredLoans);

        final List<Arrangement> filteredSavingsAccounts = filterArrangements(productSummary.getSavingsAccounts());
        productSummary.getSavingsAccounts().clear();
        productSummary.getSavingsAccounts().addAll(filteredSavingsAccounts);

        final List<Arrangement> filteredTermDeposits = filterArrangements(productSummary.getTermDeposits());
        productSummary.getTermDeposits().clear();
        productSummary.getTermDeposits().addAll(filteredTermDeposits);

        final List<Arrangement> filteredCustomProductKinds = filterArrangements(productSummary.getCustomProductKinds());
        productSummary.getCustomProductKinds().clear();
        productSummary.getCustomProductKinds().addAll(filteredCustomProductKinds);


        return productSummary;
    }

    private List<Arrangement> filterArrangements(List<Arrangement> arrangements) {
                return arrangements
                .stream()
                .filter(currencyCodePredicate) //that are not MUR
                .filter(balancePredicate)   //all balances >= threshold
                .collect(Collectors.toList());


    }
}
