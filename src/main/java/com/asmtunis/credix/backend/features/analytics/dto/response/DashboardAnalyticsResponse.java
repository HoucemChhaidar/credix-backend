package com.asmtunis.credix.backend.features.analytics.dto.response;

import java.util.List;

public class DashboardAnalyticsResponse {

	private OverviewStats overviewStats;
	private LineChartData creditDistributionOverTime;
	private BarChartData monthlyComparison;
	private PieChartData activeStatusDistribution;

	public DashboardAnalyticsResponse() {
	}

	public DashboardAnalyticsResponse(
			OverviewStats overviewStats, LineChartData creditDistributionOverTime,
			BarChartData monthlyComparison, PieChartData activeStatusDistribution
	) {
		this.overviewStats = overviewStats;
		this.creditDistributionOverTime = creditDistributionOverTime;
		this.monthlyComparison = monthlyComparison;
		this.activeStatusDistribution = activeStatusDistribution;
	}

	public OverviewStats getOverviewStats() {
		return overviewStats;
	}

	public void setOverviewStats(OverviewStats overviewStats) {
		this.overviewStats = overviewStats;
	}

	public LineChartData getCreditDistributionOverTime() {
		return creditDistributionOverTime;
	}

	public void setCreditDistributionOverTime(LineChartData creditDistributionOverTime) {
		this.creditDistributionOverTime = creditDistributionOverTime;
	}

	public BarChartData getMonthlyComparison() {
		return monthlyComparison;
	}

	public void setMonthlyComparison(BarChartData monthlyComparison) {
		this.monthlyComparison = monthlyComparison;
	}

	public PieChartData getActiveStatusDistribution() {
		return activeStatusDistribution;
	}

	public void setActiveStatusDistribution(PieChartData activeStatusDistribution) {
		this.activeStatusDistribution = activeStatusDistribution;
	}

	public static class OverviewStats {
		private Integer totalUsers;
		private Integer activeUsers;
		private Double totalCreditDistributed;
		private Double walletBalance;

		public OverviewStats() {
		}

		public OverviewStats(Integer totalUsers, Integer activeUsers, Double totalCreditDistributed, Double walletBalance) {
			this.totalUsers = totalUsers;
			this.activeUsers = activeUsers;
			this.totalCreditDistributed = totalCreditDistributed;
			this.walletBalance = walletBalance;
		}

		public Integer getTotalUsers() {
			return totalUsers;
		}

		public void setTotalUsers(Integer totalUsers) {
			this.totalUsers = totalUsers;
		}

		public Integer getActiveUsers() {
			return activeUsers;
		}

		public void setActiveUsers(Integer activeUsers) {
			this.activeUsers = activeUsers;
		}

		public Double getTotalCreditDistributed() {
			return totalCreditDistributed;
		}

		public void setTotalCreditDistributed(Double totalCreditDistributed) {
			this.totalCreditDistributed = totalCreditDistributed;
		}

		public Double getWalletBalance() {
			return walletBalance;
		}

		public void setWalletBalance(Double walletBalance) {
			this.walletBalance = walletBalance;
		}
	}

	public static class LineChartData {
		private List<String> labels;
		private List<DatasetLine> datasets;

		public LineChartData() {
		}

		public LineChartData(List<String> labels, List<DatasetLine> datasets) {
			this.labels = labels;
			this.datasets = datasets;
		}

		public List<String> getLabels() {
			return labels;
		}

		public void setLabels(List<String> labels) {
			this.labels = labels;
		}

		public List<DatasetLine> getDatasets() {
			return datasets;
		}

		public void setDatasets(List<DatasetLine> datasets) {
			this.datasets = datasets;
		}
	}

	public static class DatasetLine {
		private String label;
		private List<Double> data;

		public DatasetLine() {
		}

		public DatasetLine(String label, List<Double> data) {
			this.label = label;
			this.data = data;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public List<Double> getData() {
			return data;
		}

		public void setData(List<Double> data) {
			this.data = data;
		}
	}

	public static class BarChartData {
		private List<String> labels;
		private List<DatasetBar> datasets;

		public BarChartData() {
		}

		public BarChartData(List<String> labels, List<DatasetBar> datasets) {
			this.labels = labels;
			this.datasets = datasets;
		}

		public List<String> getLabels() {
			return labels;
		}

		public void setLabels(List<String> labels) {
			this.labels = labels;
		}

		public List<DatasetBar> getDatasets() {
			return datasets;
		}

		public void setDatasets(List<DatasetBar> datasets) {
			this.datasets = datasets;
		}
	}

	public static class DatasetBar {
		private String label;
		private List<Double> data;

		public DatasetBar() {
		}

		public DatasetBar(String label, List<Double> data) {
			this.label = label;
			this.data = data;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public List<Double> getData() {
			return data;
		}

		public void setData(List<Double> data) {
			this.data = data;
		}
	}

	public static class PieChartData {
		private List<String> labels;
		private List<Integer> data;

		public PieChartData() {
		}

		public PieChartData(List<String> labels, List<Integer> data) {
			this.labels = labels;
			this.data = data;
		}

		public List<String> getLabels() {
			return labels;
		}

		public void setLabels(List<String> labels) {
			this.labels = labels;
		}

		public List<Integer> getData() {
			return data;
		}

		public void setData(List<Integer> data) {
			this.data = data;
		}
	}
}
