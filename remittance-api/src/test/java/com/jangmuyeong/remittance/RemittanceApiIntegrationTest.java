package com.jangmuyeong.remittance;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RemittanceApiIntegrationTest {

	private final ObjectMapper om = new ObjectMapper();
	@Autowired MockMvc mvc;

	// -------------------- 1) 이체 수수료 1% --------------------
	@Test
	void remit_applies_fee_1percent_and_updates_balances() throws Exception {
		String aNo = createAccount(randomAccountNo("A"));
		String bNo = createAccount(randomAccountNo("B"));

		deposit(aNo, 1_000_000);

		JsonNode data = json(remit(aNo, bNo, 100_000, 200)).path("data");

		assertThat(data.path("fee").asLong()).isEqualTo(1_000);
		assertThat(data.path("fromBalance").asLong()).isEqualTo(899_000); // 1,000,000 - 100,000 - 1,000
		assertThat(data.path("toBalance").asLong()).isEqualTo(100_000);
	}

	// -------------------- 2) 출금 일한도 1,000,000 --------------------
	@Test
	void withdraw_daily_limit_1_000_000_is_enforced() throws Exception {
		String aNo = createAccount(randomAccountNo("W"));
		deposit(aNo, 2_000_000);

		withdraw(aNo, 900_000, 200); // OK

		JsonNode err = json(withdraw(aNo, 200_000, 400)); // 누적 1,100,000 -> FAIL
		assertThat(err.path("code").asText()).isEqualTo("WITHDRAW_DAILY_LIMIT_EXCEEDED");
	}

	// -------------------- 3) 이체 일한도 3,000,000 --------------------
	@Test
	void transfer_daily_limit_3_000_000_is_enforced() throws Exception {
		String fromNo = createAccount(randomAccountNo("F"));
		String toNo = createAccount(randomAccountNo("T"));

		deposit(fromNo, 10_000_000);

		remit(fromNo, toNo, 2_000_000, 200); // OK

		JsonNode err = json(remit(fromNo, toNo, 1_500_000, 400)); // 누적 3,500,000 -> FAIL
		assertThat(err.path("code").asText()).isEqualTo("TRANSFER_DAILY_LIMIT_EXCEEDED");
	}

	// -------------------- 4) 거래내역 최신순 + balanceAfter 검증 --------------------
	@Test
	void transactions_are_returned_in_latest_order() throws Exception {
		String aNo = createAccount(randomAccountNo("TXA"));
		String bNo = createAccount(randomAccountNo("TXB"));

		deposit(aNo, 1_000_000);
		withdraw(aNo, 200_000, 200);
		remit(aNo, bNo, 100_000, 200);

		JsonNode listA = transactions(aNo, 20);
		assertThat(listA.isArray()).isTrue();
		assertThat(listA.size()).isGreaterThanOrEqualTo(4);

		// 타입 존재 확인(구현에 따라 FEE/TRANSFER_OUT 순서는 같거나 바뀔 수 있어서 포함여부로 체크)
		String typesA = listA.toString();
		assertThat(typesA).contains("DEPOSIT");
		assertThat(typesA).contains("WITHDRAW");
		assertThat(typesA).contains("TRANSFER_OUT");
		assertThat(typesA).contains("FEE");

		// occurredAt 내림차순(최신순) 체크
		// (정렬: occurredAt desc, id desc)
		for (int i = 0; i < listA.size() - 1; i++) {
			Instant t1 = Instant.parse(listA.get(i).path("occurredAt").asText());
			Instant t2 = Instant.parse(listA.get(i + 1).path("occurredAt").asText());
			assertThat(!t1.isBefore(t2)).isTrue(); // t1 >= t2
		}

		// balanceAfter(거래 후 잔액) 체크
		// 정책: 이체는 수수료 포함 총 차감 후 잔액을 from의 TRANSFER_OUT/FEE에 기록
		boolean hasDepositAfter = false;
		boolean hasWithdrawAfter = false;
		boolean hasTransferOutAfter = false;
		boolean hasFeeAfter = false;

		for (int i = 0; i < listA.size(); i++) {
			JsonNode item = listA.get(i);
			String type = item.path("type").asText();
			long balanceAfter = item.path("balanceAfter").asLong();

			if ("DEPOSIT".equals(type)) {
				assertThat(balanceAfter).isEqualTo(1_000_000);
				hasDepositAfter = true;
			}
			if ("WITHDRAW".equals(type)) {
				assertThat(balanceAfter).isEqualTo(800_000);
				hasWithdrawAfter = true;
			}
			if ("TRANSFER_OUT".equals(type)) {
				assertThat(balanceAfter).isEqualTo(699_000); // 800,000 - 100,000 - 1,000
				hasTransferOutAfter = true;
			}
			if ("FEE".equals(type)) {
				assertThat(balanceAfter).isEqualTo(699_000); // 수수료 포함 총 차감 결과와 동일
				hasFeeAfter = true;
			}
		}

		assertThat(hasDepositAfter).isTrue();
		assertThat(hasWithdrawAfter).isTrue();
		assertThat(hasTransferOutAfter).isTrue();
		assertThat(hasFeeAfter).isTrue();

		// 수취 계좌는 TRANSFER_IN의 balanceAfter가 100,000이어야 함
		JsonNode listB = transactions(bNo, 20);
		boolean hasTransferInAfter = false;
		for (int i = 0; i < listB.size(); i++) {
			JsonNode item = listB.get(i);
			if ("TRANSFER_IN".equals(item.path("type").asText())) {
				assertThat(item.path("balanceAfter").asLong()).isEqualTo(100_000);
				hasTransferInAfter = true;
				break;
			}
		}
		assertThat(hasTransferInAfter).isTrue();

		// 현재 잔액 조회 API와 거래내역의 최신 balanceAfter가 일치해야 함
		long currentBalance = balance(aNo).path("balance").asLong();
		long latestBalanceAfter = listA.get(0).path("balanceAfter").asLong();
		assertThat(currentBalance).isEqualTo(latestBalanceAfter);
	}

	// -------------------- 5) 계좌 삭제 --------------------
	@Test
	void delete_account_then_operations_return_inactive() throws Exception {
		String aNo = createAccount(randomAccountNo("DEL"));

		deleteAccount(aNo, 200);

		JsonNode err = json(deposit(aNo, 1_000, 400));
		assertThat(err.path("code").asText()).isEqualTo("ACCOUNT_INACTIVE");
	}

	// -------------------- 6) 예외(중복/검증/없는 계좌) --------------------
	@Test
	void exceptions_duplicate_validation_not_found() throws Exception {
		String accNo = randomAccountNo("DUP");
		createAccount(accNo);

		JsonNode dup = json(createAccountExpectError(accNo, 400));
		assertThat(dup.path("code").asText()).isEqualTo("DUPLICATE_ACCOUNT_NO");

		String aNo = createAccount(randomAccountNo("VAL"));
		JsonNode invalid = json(deposit(aNo, 0, 400));
		assertThat(invalid.path("code").asText()).isEqualTo("VALIDATION_ERROR");

		String notFoundAccountNo = "NOT-FOUND-" + System.nanoTime();
		JsonNode notFound = json(deposit(notFoundAccountNo, 1000, 404));
		assertThat(notFound.path("code").asText()).isEqualTo("ACCOUNT_NOT_FOUND");
	}

	// -------------------- 7) 잔액 조회 --------------------
	@Test
	void balance_endpoint_returns_current_balance() throws Exception {
		String aNo = createAccount(randomAccountNo("BAL"));

		deposit(aNo, 50_000);
		withdraw(aNo, 10_000, 200);

		JsonNode data = balance(aNo);
		assertThat(data.path("accountNo").asText()).isEqualTo(aNo);
		assertThat(data.path("balance").asLong()).isEqualTo(40_000);
	}

	// ===================== Helpers =====================

	private String randomAccountNo(String prefix) {
		return prefix + "-" + System.nanoTime();
	}

	private JsonNode json(String body) throws Exception {
		return om.readTree(body);
	}

	private String createAccount(String accountNo) throws Exception {
		String body = om.writeValueAsString(Map.of("accountNo", accountNo));
		MvcResult result = mvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(200);

		JsonNode root = json(result.getResponse().getContentAsString());
		return root.path("data").path("accountNo").asText();
	}

	private String createAccountExpectError(String accountNo, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of("accountNo", accountNo));
		MvcResult result = mvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private String deposit(String accountNo, long amount, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of("amount", amount));
		MvcResult result = mvc.perform(post("/accounts/" + accountNo + "/deposit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private void deposit(String accountNo, long amount) throws Exception {
		deposit(accountNo, amount, 200);
	}

	private String withdraw(String accountNo, long amount, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of("amount", amount));
		MvcResult result = mvc.perform(post("/accounts/" + accountNo + "/withdraw")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private String remit(String fromAccountNo, String toAccountNo, long amount, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of(
			"fromAccountNo", fromAccountNo,
			"toAccountNo", toAccountNo,
			"amount", amount
		));
		MvcResult result = mvc.perform(post("/remittances")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private JsonNode transactions(String accountNo, int size) throws Exception {
		MvcResult result = mvc.perform(get("/accounts/" + accountNo + "/transactions")
				.param("size", String.valueOf(size)))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		return json(result.getResponse().getContentAsString()).path("data");
	}

	private void deleteAccount(String accountNo, int expectedStatus) throws Exception {
		MvcResult result = mvc.perform(delete("/accounts/" + accountNo))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
	}

	private JsonNode balance(String accountNo) throws Exception {
		MvcResult result = mvc.perform(get("/accounts/" + accountNo + "/balance"))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		return json(result.getResponse().getContentAsString()).path("data");
	}
}
