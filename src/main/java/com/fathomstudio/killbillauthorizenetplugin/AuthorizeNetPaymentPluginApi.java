/*
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.fathomstudio.killbillauthorizenetplugin;

import com.basecommercepay.client.*;
import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.*;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.entity.Pagination;
import org.osgi.service.log.LogService;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The BluePay gateway interface.
 */
public class BaseCommercePaymentPluginApi implements PaymentPluginApi {
	private static final String TYPE_CARD = "card";
	private static final String TYPE_BANK = "bank";
	
	private final Properties properties;
	private final OSGIKillbillLogService logService;
	private OSGIKillbillAPI killbillAPI;
	private OSGIKillbillDataSource dataSource;
	
	public BaseCommercePaymentPluginApi(final Properties properties, final OSGIKillbillLogService logService, final OSGIKillbillAPI killbillAPI, OSGIKillbillDataSource dataSource) {
		this.properties = properties;
		this.logService = logService;
		this.killbillAPI = killbillAPI;
		this.dataSource = dataSource;
	}
	
	@Override
	public PaymentTransactionInfoPlugin authorizePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new PaymentTransactionInfoPlugin() {
			@Override
			public UUID getKbPaymentId() {
				return kbPaymentId;
			}
			
			@Override
			public UUID getKbTransactionPaymentId() {
				return kbTransactionId;
			}
			
			@Override
			public TransactionType getTransactionType() {
				return null;
			}
			
			@Override
			public BigDecimal getAmount() {
				return null;
			}
			
			@Override
			public Currency getCurrency() {
				return null;
			}
			
			@Override
			public DateTime getCreatedDate() {
				return null;
			}
			
			@Override
			public DateTime getEffectiveDate() {
				return null;
			}
			
			@Override
			public PaymentPluginStatus getStatus() {
				return PaymentPluginStatus.CANCELED;
			}
			
			@Override
			public String getGatewayError() {
				return null;
			}
			
			@Override
			public String getGatewayErrorCode() {
				return null;
			}
			
			@Override
			public String getFirstPaymentReferenceId() {
				return null;
			}
			
			@Override
			public String getSecondPaymentReferenceId() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public PaymentTransactionInfoPlugin capturePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new PaymentTransactionInfoPlugin() {
			@Override
			public UUID getKbPaymentId() {
				return kbPaymentId;
			}
			
			@Override
			public UUID getKbTransactionPaymentId() {
				return kbTransactionId;
			}
			
			@Override
			public TransactionType getTransactionType() {
				return null;
			}
			
			@Override
			public BigDecimal getAmount() {
				return null;
			}
			
			@Override
			public Currency getCurrency() {
				return null;
			}
			
			@Override
			public DateTime getCreatedDate() {
				return null;
			}
			
			@Override
			public DateTime getEffectiveDate() {
				return null;
			}
			
			@Override
			public PaymentPluginStatus getStatus() {
				return PaymentPluginStatus.CANCELED;
			}
			
			@Override
			public String getGatewayError() {
				return null;
			}
			
			@Override
			public String getGatewayErrorCode() {
				return null;
			}
			
			@Override
			public String getFirstPaymentReferenceId() {
				return null;
			}
			
			@Override
			public String getSecondPaymentReferenceId() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	/**
	 * Called to actually make the payment.
	 *
	 * @param kbAccountId       - the account
	 * @param kbPaymentId       - the paymentID
	 * @param kbTransactionId   - the transactionId
	 * @param kbPaymentMethodId - the paymentMethodId to make the payment with
	 * @param amount            - the amount
	 * @param currency          - the currency
	 * @param properties        - properties specified by the client
	 * @param context           - the context
	 * @return
	 * @throws PaymentPluginApiException
	 */
	@Override
	public PaymentTransactionInfoPlugin purchasePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		String username;
		String password;
		String key;
		Boolean test;
		
		String credentialsQuery = "SELECT `username`, `password`, `key`, `test` FROM `baseCommerce_credentials` WHERE `tenantId` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(credentialsQuery)) {
			statement.setString(1, context.getTenantId().toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("no results");
			}
			username = resultSet.getString("username");
			password = resultSet.getString("password");
			key = resultSet.getString("key");
			test = resultSet.getBoolean("test");
			logService.log(LogService.LOG_INFO, "username: " + username);
			logService.log(LogService.LOG_INFO, "password: " + password);
			logService.log(LogService.LOG_INFO, "key: " + key);
			logService.log(LogService.LOG_INFO, "test: " + test);
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not retrieve credentials: ", e);
			throw new PaymentPluginApiException("could not retrieve credentials", e);
		}
		
		// setup the Base Commerce payment object with the given auth details
		if (username == null || username.isEmpty()) {
			throw new PaymentPluginApiException("missing username", new IllegalArgumentException());
		}
		if (password == null || password.isEmpty()) {
			throw new PaymentPluginApiException("missing password", new IllegalArgumentException());
		}
		if (key == null || key.isEmpty()) {
			throw new PaymentPluginApiException("missing key", new IllegalArgumentException());
		}
		BaseCommerceClient client = new BaseCommerceClient(username, password, key);
		if (test) {
			client.setSandbox(true);
		}
		
		// get the account associated with the ID
		final Account account;
		try {
			account = killbillAPI.getAccountUserApi().getAccountById(kbAccountId, context);
		} catch (AccountApiException e) {
			throw new RuntimeException(e);
		}
		
		String token;
		String type;
		
		String transactionIdQuery = "SELECT `token`, `type` FROM `baseCommerce_paymentMethods` WHERE `paymentMethodId` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(transactionIdQuery)) {
			statement.setString(1, kbPaymentMethodId.toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("no results");
			}
			token = resultSet.getString("token");
			type = resultSet.getString("type");
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not retrieve transaction ID: ", e);
			throw new PaymentPluginApiException("could not retrieve transaction ID", e);
		}
		
		boolean success = true;
		String code = "";
		String message = "";
		
		if (Objects.equals(type, TYPE_CARD)) {
			// prepare the transaction object
			BankCardTransaction trans = new BankCardTransaction();
			trans.setAmount(amount.doubleValue());
			trans.setType(BankCardTransaction.XS_BCT_TYPE_SALE);
			trans.setToken(token);
			
			// send the transaction to BC
			try {
				trans = client.processBankCardTransaction(trans);
			} catch (BaseCommerceClientException e) {
				success = false;
				code = e.getMessage();
				message = e.getMessage();
				logService.log(LogService.LOG_ERROR, "could not make payment: ", e);
			}
			
			// check if we did OK
			if (trans.isStatus(BankCardTransaction.XS_BCT_STATUS_FAILED)) {
				success = false;
				code = trans.getResponseCode();
				message = trans.getResponseMessage();
			} else if (trans.isStatus(BankCardTransaction.XS_BCT_STATUS_DECLINED)) {
				code = trans.getResponseCode();
				message = trans.getResponseMessage();
			} else {
				// success!
			}
		} else if (Objects.equals(type, TYPE_BANK)) {
			// prepare the transaction object
			BankAccountTransaction trans = new BankAccountTransaction();
			trans.setAmount(amount.doubleValue());
			trans.setType(BankAccountTransaction.XS_BAT_TYPE_DEBIT);
			trans.setToken(token);
			
			// send the transaction to BC
			try {
				trans = client.processBankAccountTransaction(trans);
			} catch (BaseCommerceClientException e) {
				success = false;
				code = e.getMessage();
				message = e.getMessage();
				logService.log(LogService.LOG_ERROR, "could not make payment: ", e);
			}
			
			// check if we did OK
			if (trans.isStatus(BankAccountTransaction.XS_BAT_STATUS_FAILED)) {
				String _message = "";
				for (String mess : trans.getMessages()) {
					_message += mess + " ";
				}
				success = false;
				code = message;
				message = _message;
			}
		} else {
			throw new PaymentPluginApiException("unknown type: " + type, new IllegalArgumentException());
		}
		
		// send response
		final boolean finalSuccess = success;
		final String finalMessage = message;
		final String finalCode = code;
		return new PaymentTransactionInfoPlugin() {
			@Override
			public UUID getKbPaymentId() {
				return kbPaymentId;
			}
			
			@Override
			public UUID getKbTransactionPaymentId() {
				return kbTransactionId;
			}
			
			@Override
			public TransactionType getTransactionType() {
				return TransactionType.PURCHASE;
			}
			
			@Override
			public BigDecimal getAmount() {
				return amount;
			}
			
			@Override
			public Currency getCurrency() {
				return currency;
			}
			
			@Override
			public DateTime getCreatedDate() {
				return DateTime.now();
			}
			
			@Override
			public DateTime getEffectiveDate() {
				return DateTime.now();
			}
			
			@Override
			public PaymentPluginStatus getStatus() {
				return finalSuccess ? PaymentPluginStatus.PROCESSED : PaymentPluginStatus.ERROR;
			}
			
			@Override
			public String getGatewayError() {
				return finalMessage;
			}
			
			@Override
			public String getGatewayErrorCode() {
				return finalCode;
			}
			
			@Override
			public String getFirstPaymentReferenceId() {
				return null;
			}
			
			@Override
			public String getSecondPaymentReferenceId() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public PaymentTransactionInfoPlugin voidPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new PaymentTransactionInfoPlugin() {
			@Override
			public UUID getKbPaymentId() {
				return kbPaymentId;
			}
			
			@Override
			public UUID getKbTransactionPaymentId() {
				return kbTransactionId;
			}
			
			@Override
			public TransactionType getTransactionType() {
				return null;
			}
			
			@Override
			public BigDecimal getAmount() {
				return null;
			}
			
			@Override
			public Currency getCurrency() {
				return null;
			}
			
			@Override
			public DateTime getCreatedDate() {
				return null;
			}
			
			@Override
			public DateTime getEffectiveDate() {
				return null;
			}
			
			@Override
			public PaymentPluginStatus getStatus() {
				return PaymentPluginStatus.CANCELED;
			}
			
			@Override
			public String getGatewayError() {
				return null;
			}
			
			@Override
			public String getGatewayErrorCode() {
				return null;
			}
			
			@Override
			public String getFirstPaymentReferenceId() {
				return null;
			}
			
			@Override
			public String getSecondPaymentReferenceId() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public PaymentTransactionInfoPlugin creditPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new PaymentTransactionInfoPlugin() {
			@Override
			public UUID getKbPaymentId() {
				return kbPaymentId;
			}
			
			@Override
			public UUID getKbTransactionPaymentId() {
				return kbTransactionId;
			}
			
			@Override
			public TransactionType getTransactionType() {
				return null;
			}
			
			@Override
			public BigDecimal getAmount() {
				return null;
			}
			
			@Override
			public Currency getCurrency() {
				return null;
			}
			
			@Override
			public DateTime getCreatedDate() {
				return null;
			}
			
			@Override
			public DateTime getEffectiveDate() {
				return null;
			}
			
			@Override
			public PaymentPluginStatus getStatus() {
				return PaymentPluginStatus.CANCELED;
			}
			
			@Override
			public String getGatewayError() {
				return null;
			}
			
			@Override
			public String getGatewayErrorCode() {
				return null;
			}
			
			@Override
			public String getFirstPaymentReferenceId() {
				return null;
			}
			
			@Override
			public String getSecondPaymentReferenceId() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public PaymentTransactionInfoPlugin refundPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new PaymentTransactionInfoPlugin() {
			@Override
			public UUID getKbPaymentId() {
				return kbPaymentId;
			}
			
			@Override
			public UUID getKbTransactionPaymentId() {
				return kbTransactionId;
			}
			
			@Override
			public TransactionType getTransactionType() {
				return null;
			}
			
			@Override
			public BigDecimal getAmount() {
				return null;
			}
			
			@Override
			public Currency getCurrency() {
				return null;
			}
			
			@Override
			public DateTime getCreatedDate() {
				return null;
			}
			
			@Override
			public DateTime getEffectiveDate() {
				return null;
			}
			
			@Override
			public PaymentPluginStatus getStatus() {
				return PaymentPluginStatus.CANCELED;
			}
			
			@Override
			public String getGatewayError() {
				return null;
			}
			
			@Override
			public String getGatewayErrorCode() {
				return null;
			}
			
			@Override
			public String getFirstPaymentReferenceId() {
				return null;
			}
			
			@Override
			public String getSecondPaymentReferenceId() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public List<PaymentTransactionInfoPlugin> getPaymentInfo(final UUID kbAccountId, final UUID kbPaymentId, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
		// not implemented
		return Collections.emptyList();
	}
	
	@Override
	public Pagination<PaymentTransactionInfoPlugin> searchPayments(final String searchKey, final Long offset, final Long limit, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
		// not implemented
		return new Pagination<PaymentTransactionInfoPlugin>() {
			@Override
			public Long getCurrentOffset() {
				return null;
			}
			
			@Override
			public Long getNextOffset() {
				return null;
			}
			
			@Override
			public Long getMaxNbRecords() {
				return null;
			}
			
			@Override
			public Long getTotalNbRecords() {
				return null;
			}
			
			@Override
			public Iterator<PaymentTransactionInfoPlugin> iterator() {
				return null;
			}
		};
	}
	
	/**
	 * Create a payment method with the given details.
	 *
	 * @param kbAccountId        - the account
	 * @param kbPaymentMethodId  - the paymentMethodId
	 * @param paymentMethodProps - the properties
	 * @param setDefault         - if this should be the default
	 * @param properties         - client-specified properties
	 * @param context            - the context
	 * @throws PaymentPluginApiException
	 */
	@Override
	public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		String username;
		String password;
		String key;
		Boolean test;
		
		String credentialsQuery = "SELECT `username`, `password`, `key`, `test` FROM `baseCommerce_credentials` WHERE `tenantId` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(credentialsQuery)) {
			statement.setString(1, context.getTenantId().toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("no results");
			}
			username = resultSet.getString("username");
			password = resultSet.getString("password");
			key = resultSet.getString("key");
			test = resultSet.getBoolean("test");
			logService.log(LogService.LOG_INFO, "username: " + username);
			logService.log(LogService.LOG_INFO, "password: " + password);
			logService.log(LogService.LOG_INFO, "key: " + key);
			logService.log(LogService.LOG_INFO, "test: " + test);
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not retrieve credentials: ", e);
			throw new PaymentPluginApiException("could not retrieve credentials", e);
		}
		
		// setup the Base Commerce payment object with the given auth details
		if (username == null || username.isEmpty()) {
			throw new PaymentPluginApiException("missing username", new IllegalArgumentException());
		}
		if (password == null || password.isEmpty()) {
			throw new PaymentPluginApiException("missing password", new IllegalArgumentException());
		}
		if (key == null || key.isEmpty()) {
			throw new PaymentPluginApiException("missing key", new IllegalArgumentException());
		}
		BaseCommerceClient client = new BaseCommerceClient(username, password, key);
		if (test) {
			client.setSandbox(true);
		}
		
		String paymentType = null;
		
		String creditCardNumber = null;
		String creditCardCVV2 = null;
		String creditCardExpirationMonth = null;
		String creditCardExpirationYear = null;
		
		String routingNumber = null;
		String accountNumber = null;
		
		// get the client-passed properties including BluePay auth details and appropriate credit card or ACH details
		for (PluginProperty property : paymentMethodProps.getProperties()) {
			String kv_key = property.getKey();
			Object value = property.getValue();
			logService.log(LogService.LOG_INFO, "key: " + kv_key);
			logService.log(LogService.LOG_INFO, "value: " + value);
			if (Objects.equals(kv_key, "paymentType")) {
				logService.log(LogService.LOG_INFO, "setting paymentType");
				paymentType = value.toString();
			} else if (Objects.equals(kv_key, "creditCardNumber")) {
				creditCardNumber = value.toString();
			} else if (Objects.equals(kv_key, "creditCardCVV2")) {
				creditCardCVV2 = value.toString();
			} else if (Objects.equals(kv_key, "creditCardExpirationMonth")) {
				creditCardExpirationMonth = value.toString();
			} else if (Objects.equals(kv_key, "creditCardExpirationYear")) {
				creditCardExpirationYear = value.toString();
			} else if (Objects.equals(kv_key, "routingNumber")) {
				routingNumber = value.toString();
			} else if (Objects.equals(kv_key, "accountNumber")) {
				accountNumber = value.toString();
			} else {
				throw new PaymentPluginApiException("unrecognized plugin property: " + kv_key, new IllegalArgumentException());
			}
		}
		
		// get the account object for the account ID
		final Account account;
		try {
			account = killbillAPI.getAccountUserApi().getAccountById(kbAccountId, context);
		} catch (AccountApiException e) {
			logService.log(LogService.LOG_ERROR, "could not retrieve account: ", e);
			throw new PaymentPluginApiException("could not retrieve account", e);
		}
		
		// setup the customer that will be associated with this token
		/*HashMap<String, String> customer = new HashMap<>();
		String firstName = account.getName() == null ? null : account.getName().substring(0, account.getFirstNameLength());
		String lastName = account.getName() == null ? null : account.getName().substring(account.getFirstNameLength());
		logService.log(LogService.LOG_INFO, "firstName: " + firstName);
		logService.log(LogService.LOG_INFO, "lastName: " + lastName);
		customer.put("firstName", firstName);
		customer.put("lastName", lastName);
		customer.put("address1", account.getAddress1());
		customer.put("address2", account.getAddress2());
		customer.put("city", account.getCity());
		customer.put("state", account.getStateOrProvince());
		customer.put("zip", account.getPostalCode());
		customer.put("country", account.getCountry());
		customer.put("phone", account.getPhone());
		customer.put("email", account.getEmail());
		bluePay.setCustomerInformation(customer);*/
		
		String token;
		String type;
		
		// setup paymentType-specific payment details
		if (paymentType == null || paymentType.isEmpty()) {
			throw new PaymentPluginApiException("missing paymentType", new IllegalArgumentException());
		}
		if (Objects.equals(paymentType, "card")) { // credit card
			if (creditCardNumber == null || creditCardNumber.isEmpty()) {
				throw new PaymentPluginApiException("missing creditCardNumber", new IllegalArgumentException());
			}
			if (creditCardExpirationMonth == null || creditCardExpirationMonth.isEmpty()) {
				throw new PaymentPluginApiException("missing creditCardExpirationMonth", new IllegalArgumentException());
			}
			if (creditCardExpirationYear == null || creditCardExpirationYear.isEmpty()) {
				throw new PaymentPluginApiException("missing creditCardExpirationYear", new IllegalArgumentException());
			}
			if (creditCardCVV2 == null || creditCardCVV2.isEmpty()) {
				throw new PaymentPluginApiException("missing creditCardCVV2", new IllegalArgumentException());
			}
			
			String twoDigitMonth = creditCardExpirationMonth;
			if (twoDigitMonth.length() == 1) {
				twoDigitMonth = "0" + twoDigitMonth;
			}
			
			BankCard card = new BankCard();
			card.setExpirationMonth(twoDigitMonth);
			card.setExpirationYear("20" + creditCardExpirationYear);
			card.setNumber(creditCardNumber);
			card.setName("Card " + creditCardNumber.substring(creditCardNumber.length() - 4));
			try {
				card = client.addBankCard(card);
			} catch (BaseCommerceClientException e) {
				logService.log(LogService.LOG_ERROR, "error while saving bank card: ", e);
				throw new PaymentPluginApiException("error while saving bank card", e);
			}
			if (card.isStatus(BankCard.XS_BC_STATUS_FAILED) ) {
				String message = "";
				for (String mess : card.getMessages()) {
					message += mess + " ";
				}
				logService.log(LogService.LOG_ERROR, "error while saving bank card: ", new Exception(message));
				throw new PaymentPluginApiException("error while saving bank card", new Exception(message));
			}
			token = card.getToken();
			type = TYPE_CARD;
		} else if (Objects.equals(paymentType, "ach")) { // ACH
			if (routingNumber == null) {
				throw new PaymentPluginApiException("missing routingNumber", new IllegalArgumentException());
			}
			if (accountNumber == null) {
				throw new PaymentPluginApiException("missing accountNumber", new IllegalArgumentException());
			}
			
			BankAccount bank = new BankAccount();
			bank.setRoutingNumber(routingNumber);
			bank.setAccountNumber(accountNumber);
			bank.setType(BankAccount.XS_BA_TYPE_CHECKING);
			bank.setName("Bank " + accountNumber.substring(accountNumber.length() - 4));
			try {
				bank = client.addBankAccount(bank);
			} catch (BaseCommerceClientException e) {
				logService.log(LogService.LOG_ERROR, "error while saving bank account: ", e);
				throw new PaymentPluginApiException("error while saving bank account", e);
			}
			if (bank.isStatus(BankAccount.XS_BA_STATUS_FAILED) ) {
				String message = "";
				for (String mess : bank.getMessages()) {
					message += mess + " ";
				}
				logService.log(LogService.LOG_ERROR, "error while saving bank account: ", new Exception(message));
				throw new PaymentPluginApiException("error while saving bank account", new Exception(message));
			}
			token = bank.getToken();
			type = TYPE_BANK;
		} else {
			throw new PaymentPluginApiException("unknown paymentType: " + paymentType, new IllegalArgumentException());
		}
		
		String transactionIdQuery = "INSERT INTO `baseCommerce_paymentMethods` (`paymentMethodId`, `token`, `type`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `paymentMethodId` = ?, `token` = ?, `type` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(transactionIdQuery)) {
			statement.setString(1, kbPaymentMethodId.toString());
			statement.setString(2, token);
			statement.setString(3, type);
			statement.setString(4, kbPaymentMethodId.toString());
			statement.setString(5, token);
			statement.setString(6, type);
			statement.executeUpdate();
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not save token: ", e);
			throw new PaymentPluginApiException("could not save token", e);
		}
	}
	
	@Override
	public void deletePaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
	}
	
	@Override
	public PaymentMethodPlugin getPaymentMethodDetail(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
		// not implemented
		return new PaymentMethodPlugin() {
			@Override
			public UUID getKbPaymentMethodId() {
				return kbPaymentMethodId;
			}
			
			@Override
			public String getExternalPaymentMethodId() {
				return null;
			}
			
			@Override
			public boolean isDefaultPaymentMethod() {
				return false;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public void setDefaultPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
	}
	
	@Override
	public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return Collections.emptyList();
	}
	
	@Override
	public Pagination<PaymentMethodPlugin> searchPaymentMethods(final String searchKey, final Long offset, final Long limit, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
		// not implemented
		return new Pagination<PaymentMethodPlugin>() {
			@Override
			public Long getCurrentOffset() {
				return null;
			}
			
			@Override
			public Long getNextOffset() {
				return null;
			}
			
			@Override
			public Long getMaxNbRecords() {
				return null;
			}
			
			@Override
			public Long getTotalNbRecords() {
				return null;
			}
			
			@Override
			public Iterator<PaymentMethodPlugin> iterator() {
				return null;
			}
		};
	}
	
	@Override
	public void resetPaymentMethods(final UUID kbAccountId, final List<PaymentMethodInfoPlugin> paymentMethods, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
	}
	
	@Override
	public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID kbAccountId, final Iterable<PluginProperty> customFields, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new HostedPaymentPageFormDescriptor() {
			@Override
			public UUID getKbAccountId() {
				return kbAccountId;
			}
			
			@Override
			public String getFormMethod() {
				return null;
			}
			
			@Override
			public String getFormUrl() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getFormFields() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
	
	@Override
	public GatewayNotification processNotification(final String notification, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
		// not implemented
		return new GatewayNotification() {
			@Override
			public UUID getKbPaymentId() {
				return null;
			}
			
			@Override
			public int getStatus() {
				return 0;
			}
			
			@Override
			public String getEntity() {
				return null;
			}
			
			@Override
			public Map<String, List<String>> getHeaders() {
				return null;
			}
			
			@Override
			public List<PluginProperty> getProperties() {
				return null;
			}
		};
	}
}
