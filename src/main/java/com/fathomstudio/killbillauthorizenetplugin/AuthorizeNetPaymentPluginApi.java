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

import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateCustomerProfileController;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
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
public class AuthorizeNetPaymentPluginApi implements PaymentPluginApi {
	private static final String TYPE_CARD = "card";
	private static final String TYPE_BANK = "bank";
	private final Object envLock = new Object();
	
	private final Properties properties;
	private final OSGIKillbillLogService logService;
	private OSGIKillbillAPI killbillAPI;
	private OSGIKillbillDataSource dataSource;
	
	public AuthorizeNetPaymentPluginApi(final Properties properties, final OSGIKillbillLogService logService, final OSGIKillbillAPI killbillAPI, OSGIKillbillDataSource dataSource) {
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
		
		// charge a customer profile
		// https://developer.authorize.net/api/reference/index.html#payment-transactions-charge-a-customer-profile
		
		String loginId;
		String transactionKey;
		Boolean test;
		
		String credentialsQuery = "SELECT `loginId`, `transactionKey`, `test` FROM `authorizeNet_credentials` WHERE `tenantId` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(credentialsQuery)) {
			statement.setString(1, context.getTenantId().toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("no results");
			}
			loginId = resultSet.getString("loginId");
			transactionKey = resultSet.getString("transactionKey");
			test = resultSet.getBoolean("test");
			logService.log(LogService.LOG_INFO, "loginId: " + loginId);
			logService.log(LogService.LOG_INFO, "transactionKey: " + transactionKey);
			logService.log(LogService.LOG_INFO, "test: " + test);
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not retrieve credentials: ", e);
			throw new PaymentPluginApiException("could not retrieve credentials", e);
		}
		
		if (loginId == null || loginId.isEmpty()) {
			throw new PaymentPluginApiException("missing loginId", new IllegalArgumentException());
		}
		if (transactionKey == null || transactionKey.isEmpty()) {
			throw new PaymentPluginApiException("missing transactionKey", new IllegalArgumentException());
		}
		
		boolean success = true;
		String code = "";
		String message = "";
		
		synchronized (envLock) {
			// Set the request to operate in either the sandbox or production environment
			ApiOperationBase.setEnvironment(test ? Environment.SANDBOX : Environment.PRODUCTION);
			
			// Create object with merchant authentication details
			MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
			merchantAuthenticationType.setName(loginId);
			merchantAuthenticationType.setTransactionKey(transactionKey);
			
			// get the account associated with the ID
			final Account account;
			try {
				account = killbillAPI.getAccountUserApi().getAccountById(kbAccountId, context);
			} catch (AccountApiException e) {
				throw new RuntimeException(e);
			}
			
			String customerProfileId;
			String type;
			
			String transactionIdQuery = "SELECT `customerProfileId`, `type` FROM `authorizeNet_paymentMethods` WHERE `paymentMethodId` = ?";
			try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(transactionIdQuery)) {
				statement.setString(1, kbPaymentMethodId.toString());
				ResultSet resultSet = statement.executeQuery();
				if (!resultSet.next()) {
					throw new SQLException("no results");
				}
				customerProfileId = resultSet.getString("customerProfileId");
				type = resultSet.getString("type");
			} catch (SQLException e) {
				logService.log(LogService.LOG_ERROR, "could not retrieve transaction ID: ", e);
				throw new PaymentPluginApiException("could not retrieve transaction ID", e);
			}
			
			// Set the profile ID to charge
			CustomerProfilePaymentType profileToCharge = new CustomerProfilePaymentType();
			profileToCharge.setCustomerProfileId(customerProfileId);
			PaymentProfile paymentProfile = new PaymentProfile();
			paymentProfile.setPaymentProfileId(customerProfileId);
			profileToCharge.setPaymentProfile(paymentProfile);
			
			// Create the payment transaction request
			TransactionRequestType txnRequest = new TransactionRequestType();
			txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
			txnRequest.setProfile(profileToCharge);
			txnRequest.setAmount(amount);
			
			CreateTransactionRequest apiRequest = new CreateTransactionRequest();
			apiRequest.setTransactionRequest(txnRequest);
			CreateTransactionController controller = new CreateTransactionController(apiRequest);
			controller.execute();
			
			
			CreateTransactionResponse response = controller.getApiResponse();
			
			if (response != null) {
				// If API Response is ok, go ahead and check the transaction response
				if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
					TransactionResponse result = response.getTransactionResponse();
					if (result.getMessages() != null) {
						logService.log(LogService.LOG_INFO, "Successfully created transaction with Transaction ID: " + result.getTransId());
						logService.log(LogService.LOG_INFO, "Response Code: " + result.getResponseCode());
						logService.log(LogService.LOG_INFO, "Message Code: " + result.getMessages().getMessage().get(0).getCode());
						logService.log(LogService.LOG_INFO, "Description: " + result.getMessages().getMessage().get(0).getDescription());
						logService.log(LogService.LOG_INFO, "Auth Code: " + result.getAuthCode());
					} else {
						if (response.getTransactionResponse().getErrors() != null) {
							code = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode();
							message = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();
							success = false;
						}
					}
				} else {
					if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
						code = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode();
						message = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();
					} else {
						code = response.getMessages().getMessage().get(0).getCode();
						message = response.getMessages().getMessage().get(0).getText();
					}
					success = false;
				}
			} else {
				message = "Null Response.";
				success = false;
			}
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
		
		// create a customer profile
		// https://developer.authorize.net/api/reference/index.html#customer-profiles-create-customer-profile
		
		String loginId;
		String transactionKey;
		Boolean test;
		
		String credentialsQuery = "SELECT `loginId`, `transactionKey`, `test` FROM `authorizeNet_credentials` WHERE `tenantId` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(credentialsQuery)) {
			statement.setString(1, context.getTenantId().toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("no results");
			}
			loginId = resultSet.getString("loginId");
			transactionKey = resultSet.getString("transactionKey");
			test = resultSet.getBoolean("test");
			logService.log(LogService.LOG_INFO, "loginId: " + loginId);
			logService.log(LogService.LOG_INFO, "transactionKey: " + transactionKey);
			logService.log(LogService.LOG_INFO, "test: " + test);
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not retrieve credentials: ", e);
			throw new PaymentPluginApiException("could not retrieve credentials", e);
		}
		
		if (loginId == null || loginId.isEmpty()) {
			throw new PaymentPluginApiException("missing loginId", new IllegalArgumentException());
		}
		if (transactionKey == null || transactionKey.isEmpty()) {
			throw new PaymentPluginApiException("missing transactionKey", new IllegalArgumentException());
		}
		
		String customerProfileId;
		String type;
		
		synchronized (envLock) {
			// Set the request to operate in either the sandbox or production environment
			ApiOperationBase.setEnvironment(test ? Environment.SANDBOX : Environment.PRODUCTION);
			
			// Create object with merchant authentication details
			MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
			merchantAuthenticationType.setName(loginId);
			merchantAuthenticationType.setTransactionKey(transactionKey);
			
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
			
			PaymentType paymentTypeType = new PaymentType();
			
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
				
				// Populate the payment data
				CreditCardType creditCard = new CreditCardType();
				creditCard.setCardNumber(creditCardNumber);
				creditCard.setCardCode(creditCardCVV2);
				creditCard.setExpirationDate(twoDigitMonth + creditCardExpirationYear);
				paymentTypeType.setCreditCard(creditCard);
				
				type = TYPE_CARD;
			} else if (Objects.equals(paymentType, "ach")) { // ACH
				if (routingNumber == null) {
					throw new PaymentPluginApiException("missing routingNumber", new IllegalArgumentException());
				}
				if (accountNumber == null) {
					throw new PaymentPluginApiException("missing accountNumber", new IllegalArgumentException());
				}
				
				BankAccountType bankAccount = new BankAccountType();
				bankAccount.setRoutingNumber(routingNumber);
				bankAccount.setAccountNumber(accountNumber);
				bankAccount.setAccountType(BankAccountTypeEnum.CHECKING);
				bankAccount.setNameOnAccount(account.getName());
				paymentTypeType.setBankAccount(bankAccount);
				
				type = TYPE_BANK;
			} else {
				throw new PaymentPluginApiException("unknown paymentType: " + paymentType, new IllegalArgumentException());
			}
			
			// Set payment profile data
			CustomerPaymentProfileType customerPaymentProfileType = new CustomerPaymentProfileType();
			customerPaymentProfileType.setCustomerType(CustomerTypeEnum.INDIVIDUAL);
			customerPaymentProfileType.setPayment(paymentTypeType);
			
			// Set customer profile data
			CustomerProfileType customerProfileType = new CustomerProfileType();
			customerProfileType.setMerchantCustomerId("M_" + account.getEmail());
			customerProfileType.setDescription("Profile description for " + account.getEmail());
			customerProfileType.setEmail(account.getEmail());
			customerProfileType.getPaymentProfiles().add(customerPaymentProfileType);
			
			// Create the API request and set the parameters for this specific request
			CreateCustomerProfileRequest apiRequest = new CreateCustomerProfileRequest();
			apiRequest.setMerchantAuthentication(merchantAuthenticationType);
			apiRequest.setProfile(customerProfileType);
			apiRequest.setValidationMode(test ? ValidationModeEnum.TEST_MODE : ValidationModeEnum.LIVE_MODE);
			
			// Call the controller
			CreateCustomerProfileController controller = new CreateCustomerProfileController(apiRequest);
			controller.execute();
			
			// Get the response
			CreateCustomerProfileResponse response = controller.getApiResponse();
			
			// Parse the response to determine results
			if (response != null) {
				// If API Response is OK, go ahead and check the transaction response
				if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
					customerProfileId = response.getCustomerProfileId();
					/*if (!response.getCustomerPaymentProfileIdList().getNumericString().isEmpty()) {
						customerProfileId = response.getCustomerPaymentProfileIdList().getNumericString().get(0);
					} else {
						logService.log(LogService.LOG_ERROR, "no customerProfileId: ", new Exception());
						throw new PaymentPluginApiException("no customerProfileId", new Exception());
					}*/
					/*if (!response.getCustomerShippingAddressIdList().getNumericString().isEmpty()) {
						ystem.out.println(response.getCustomerShippingAddressIdList().getNumericString().get(0));
					}
					if (!response.getValidationDirectResponseList().getString().isEmpty()) {
						ystem.out.println(response.getValidationDirectResponseList().getString().get(0));
					}*/
				} else {
					String message = "Failed to create customer profile:  " + response.getMessages().getResultCode();
					logService.log(LogService.LOG_ERROR, "error while creating customer profile: ", new Exception(message));
					throw new PaymentPluginApiException("error while creating customer profile", new Exception(message));
				}
			} else {
				// Display the error code and message when response is null 
				ANetApiResponse errorResponse = controller.getErrorResponse();
				String message = "unknown";
				if (!errorResponse.getMessages().getMessage().isEmpty()) {
					message = "Error: " + errorResponse.getMessages().getMessage().get(0).getCode() + " \n" + errorResponse.getMessages().getMessage().get(0).getText();
				}
				logService.log(LogService.LOG_ERROR, "error while creating customer profile: ", new Exception(message));
				throw new PaymentPluginApiException("error while creating customer profile", new Exception(message));
			}
		}
		
		String transactionIdQuery = "INSERT INTO `authorizeNet_paymentMethods` (`paymentMethodId`, `customerProfileId`, `type`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `paymentMethodId` = ?, `customerProfileId` = ?, `type` = ?";
		try (PreparedStatement statement = dataSource.getDataSource().getConnection().prepareStatement(transactionIdQuery)) {
			statement.setString(1, kbPaymentMethodId.toString());
			statement.setString(2, customerProfileId);
			statement.setString(3, type);
			statement.setString(4, kbPaymentMethodId.toString());
			statement.setString(5, customerProfileId);
			statement.setString(6, type);
			statement.executeUpdate();
		} catch (SQLException e) {
			logService.log(LogService.LOG_ERROR, "could not save customerProfileId: ", e);
			throw new PaymentPluginApiException("could not save customerProfileId", e);
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
