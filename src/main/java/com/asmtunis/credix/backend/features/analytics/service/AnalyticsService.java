package com.asmtunis.credix.backend.features.analytics.service;

import com.asmtunis.credix.backend.features.analytics.dto.response.DashboardAnalyticsResponse;
import com.asmtunis.credix.backend.features.analytics.dto.response.DashboardAnalyticsResponse.*;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionType;
import com.asmtunis.credix.backend.features.transaction.repository.TransactionRepository;
import com.asmtunis.credix.backend.features.user.entity.Role;
import com.asmtunis.credix.backend.features.user.entity.User;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import com.asmtunis.credix.backend.features.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

	private final UserRepository userRepository;
	private final WalletRepository walletRepository;
	private final TransactionRepository transactionRepository;

	public AnalyticsService(
			UserRepository userRepository,
			WalletRepository walletRepository,
			TransactionRepository transactionRepository
	) {
		this.userRepository = userRepository;
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
	}

	/**
	 * Get dashboard analytics for ADMIN or SUPER_ADMIN
	 */
	public DashboardAnalyticsResponse getDashboardAnalytics(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

		if (user.getRole() == Role.ADMIN) {
			return getAdminDashboardAnalytics(user);
		} else if (user.getRole() == Role.SUPER_ADMIN) {
			return getSuperAdminDashboardAnalytics(user);
		} else {
			throw new RuntimeException("Unauthorized: Only ADMIN and SUPER_ADMIN can access analytics");
		}
	}

	/**
	 * Get analytics for ADMIN dashboard
	 */
	private DashboardAnalyticsResponse getAdminDashboardAnalytics(User admin) {
		// Overview Stats
		List<User> managedUsers = userRepository.findByAdminAndActiveTrue(admin);
		int totalUsers = managedUsers.size();
		int activeUsers = (int) managedUsers.stream().filter(User::getActive).count();

		Double totalCreditDistributed = transactionRepository.findCreditTransactionsByAdminId(admin.getId())
				.stream()
				.mapToDouble(t -> t.getAmount())
				.sum();

		Double walletBalance = walletRepository.findByUserId(admin.getId())
				.map(w -> w.getBalance())
				.orElse(0.0);

		OverviewStats overviewStats = new OverviewStats(totalUsers, activeUsers, totalCreditDistributed, walletBalance);

		// Line Chart & Bar Chart - Credit Distribution Over Time (last 7 months)
		LineChartData lineChartData = getCreditDistributionOverTime(admin.getId());
		BarChartData barChartData = getMonthlyComparisonData(admin.getId());

		// Pie Chart - Active vs Inactive Users
		PieChartData pieChartData = new PieChartData(
				Arrays.asList("Active", "Inactive"),
				Arrays.asList(activeUsers, totalUsers - activeUsers)
		);

		return new DashboardAnalyticsResponse(overviewStats, lineChartData, barChartData, pieChartData);
	}

	/**
	 * Get analytics for SUPER_ADMIN dashboard
	 */
	private DashboardAnalyticsResponse getSuperAdminDashboardAnalytics(User superAdmin) {
		// Find all admins managed by this super admin
		List<User> managedAdmins = userRepository.findByAdminAndActiveTrue(superAdmin);
		int totalAdmins = managedAdmins.size();
		int activeAdmins = (int) managedAdmins.stream().filter(User::getActive).count();

		// Calculate total users across all admins
		int totalSystemUsers = managedAdmins.stream()
				.mapToInt(admin -> userRepository.findByAdminAndActiveTrue(admin).size())
				.sum();

		Double totalCreditDistributed = transactionRepository.findCreditTransactionsByAdminId(superAdmin.getId())
				.stream()
				.mapToDouble(t -> t.getAmount())
				.sum();

		Double walletBalance = walletRepository.findByUserId(superAdmin.getId())
				.map(w -> w.getBalance())
				.orElse(0.0);

		OverviewStats overviewStats = new OverviewStats(totalAdmins, activeAdmins, totalCreditDistributed, (double) totalSystemUsers);

		// Line Chart & Bar Chart - Credit Distribution Over Time (last 7 months)
		LineChartData lineChartData = getCreditDistributionOverTime(superAdmin.getId());
		BarChartData barChartData = getMonthlyComparisonData(superAdmin.getId());

		// Pie Chart - Active vs Inactive Admins
		PieChartData pieChartData = new PieChartData(
				Arrays.asList("Active", "Inactive"),
				Arrays.asList(activeAdmins, totalAdmins - activeAdmins)
		);

		return new DashboardAnalyticsResponse(overviewStats, lineChartData, barChartData, pieChartData);
	}

	/**
	 * Get credit distribution over last 7 months for line chart
	 */
	private LineChartData getCreditDistributionOverTime(UUID adminId) {
		LocalDateTime now = LocalDateTime.now();
		List<String> monthLabels = new ArrayList<>();
		List<Double> creditData = new ArrayList<>();
		List<Double> balanceData = new ArrayList<>();

		// Generate last 7 months
		for (int i = 6; i >= 0; i--) {
			LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
			LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

			String monthLabel = monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
			monthLabels.add(monthLabel);

			// Get credit transactions for this month
			Double monthlyCredit = transactionRepository.findByUserIdAndDateRange(adminId, monthStart, monthEnd)
					.stream()
					.filter(t -> t.getType() == TransactionType.CREDIT_ADDITION)
					.mapToDouble(t -> t.getAmount())
					.sum();

			creditData.add(monthlyCredit);

			// For balance line, we'll use cumulative amounts or variations
			balanceData.add(monthlyCredit * 0.7); // Mock data for second line
		}

		List<DatasetLine> datasets = Arrays.asList(
				new DatasetLine("Credit Additions", creditData),
				new DatasetLine("Balance Changes", balanceData)
		);

		return new LineChartData(monthLabels, datasets);
	}

	/**
	 * Get monthly comparison data for bar chart
	 */
	private BarChartData getMonthlyComparisonData(UUID adminId) {
		LocalDateTime now = LocalDateTime.now();
		List<String> monthLabels = new ArrayList<>();
		List<Double> dataset1 = new ArrayList<>();
		List<Double> dataset2 = new ArrayList<>();

		// Generate last 7 months
		for (int i = 6; i >= 0; i--) {
			LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
			LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

			String monthLabel = monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
			monthLabels.add(monthLabel);

			// Get credit transactions for this month
			Double monthlyCredit = transactionRepository.findByUserIdAndDateRange(adminId, monthStart, monthEnd)
					.stream()
					.filter(t -> t.getType() == TransactionType.CREDIT_ADDITION)
					.mapToDouble(t -> t.getAmount())
					.sum();

			dataset1.add(monthlyCredit);

			// Mock data for second dataset (could represent different metric)
			dataset2.add(monthlyCredit * 0.5);
		}

		List<DatasetBar> datasets = Arrays.asList(
				new DatasetBar("Credits Given", dataset1),
				new DatasetBar("Credits Used", dataset2)
		);

		return new BarChartData(monthLabels, datasets);
	}
}
