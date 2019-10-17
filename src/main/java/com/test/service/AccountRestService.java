package com.test.service;

import com.test.model.Account;
import com.test.service.impl.H2AccountService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Collection;

@Path("/account")
public class AccountRestService {
    //TODO move to constructor arg
    private AccountService accountService = H2AccountService.INSTANCE;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Account> list() {
        return accountService.list();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Account get(@QueryParam("id") int id) {
        return accountService.get(id);
    }

    @PUT
    public void add(@FormParam("amount") BigDecimal amount) {
        Account account = new Account();
        account.setAmount(amount);
        accountService.add(account);
    }

    @POST
    public void update(@FormParam("id") int id,
                       @FormParam("amount") BigDecimal amount) {
        Account account = new Account();
        account.setId(id);
        account.setAmount(amount);
        accountService.update(account);
    }

    @DELETE
    public void delete(@QueryParam("id") int id) {
        accountService.delete(id);
    }

    @POST
    @Path("transfer")
    public void transfer(@FormParam("fromId") int fromId,
                         @FormParam("toId") int toId,
                         @FormParam("amount") BigDecimal amount) throws InsufficientFundException {
        //TODO NPE
        //TODO map exceptions
        accountService.transfer(accountService.get(fromId), accountService.get(toId), amount);
    }
}
