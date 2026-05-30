package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {
    private int openTaskCount;
    private int myTaskCount;
    private int activeScheduleCount;
    private int housemateCount;
    private List<TaskDetailDto> myPendingTasks;
    private List<CleaningScheduleDetailDto> householdSchedules;
    private List<ShoppingItemDto> wgShoppingList;
    private BigDecimal netBalance;
    private List<BalanceDto> whoOwesMe;
    private List<BalanceDto> whoIowe;

    public DashboardDto() {
    }

    public DashboardDto(int openTaskCount, int myTaskCount, int activeScheduleCount, int housemateCount,
                        List<TaskDetailDto> myPendingTasks, List<CleaningScheduleDetailDto> householdSchedules,
                        List<ShoppingItemDto> wgShoppingList, BigDecimal netBalance,
                        List<BalanceDto> whoOwesMe, List<BalanceDto> whoIowe) {
        this.openTaskCount = openTaskCount;
        this.myTaskCount = myTaskCount;
        this.activeScheduleCount = activeScheduleCount;
        this.housemateCount = housemateCount;
        this.myPendingTasks = myPendingTasks;
        this.householdSchedules = householdSchedules;
        this.wgShoppingList = wgShoppingList;
        this.netBalance = netBalance;
        this.whoOwesMe = whoOwesMe;
        this.whoIowe = whoIowe;
    }

    public int getOpenTaskCount() {
        return openTaskCount;
    }

    public void setOpenTaskCount(int openTaskCount) {
        this.openTaskCount = openTaskCount;
    }

    public int getMyTaskCount() {
        return myTaskCount;
    }

    public void setMyTaskCount(int myTaskCount) {
        this.myTaskCount = myTaskCount;
    }

    public int getActiveScheduleCount() {
        return activeScheduleCount;
    }

    public void setActiveScheduleCount(int activeScheduleCount) {
        this.activeScheduleCount = activeScheduleCount;
    }

    public int getHousemateCount() {
        return housemateCount;
    }

    public void setHousemateCount(int housemateCount) {
        this.housemateCount = housemateCount;
    }

    public List<TaskDetailDto> getMyPendingTasks() {
        return myPendingTasks;
    }

    public void setMyPendingTasks(List<TaskDetailDto> myPendingTasks) {
        this.myPendingTasks = myPendingTasks;
    }

    public List<CleaningScheduleDetailDto> getHouseholdSchedules() {
        return householdSchedules;
    }

    public void setHouseholdSchedules(List<CleaningScheduleDetailDto> householdSchedules) {
        this.householdSchedules = householdSchedules;
    }

    public List<ShoppingItemDto> getWgShoppingList() {
        return wgShoppingList;
    }

    public void setWgShoppingList(List<ShoppingItemDto> wgShoppingList) {
        this.wgShoppingList = wgShoppingList;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public List<BalanceDto> getWhoOwesMe() {
        return whoOwesMe;
    }

    public void setWhoOwesMe(List<BalanceDto> whoOwesMe) {
        this.whoOwesMe = whoOwesMe;
    }

    public List<BalanceDto> getWhoIowe() {
        return whoIowe;
    }

    public void setWhoIowe(List<BalanceDto> whoIowe) {
        this.whoIowe = whoIowe;
    }
}
