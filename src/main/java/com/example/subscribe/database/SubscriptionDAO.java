package com.example.subscribe.database;

import com.example.subscribe.models.Subscription;
import com.example.subscribe.models.Category;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class SubscriptionDAO {

    private final Connection conn;

    public SubscriptionDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Subscription> getAll() {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Subscription s = new Subscription();
                s.setId(rs.getLong("id"));
                s.setName(rs.getString("name"));
                s.setCost(BigDecimal.valueOf(rs.getDouble("cost")));
                s.setCurrency(rs.getString("currency"));
                s.setStartDate(rs.getString("start_date") != null ? LocalDate.parse(rs.getString("start_date")) : null);
                s.setNextPaymentDate(rs.getString("next_payment_date") != null ? LocalDate.parse(rs.getString("next_payment_date")) : null);
                s.setBillingCycle(rs.getInt("billing_cycle"));
                s.setCategory(Category.fromDisplayName(rs.getString("category")));
                s.setActive(rs.getInt("active") == 1);
                s.setDescription(rs.getString("description"));
                s.setWebsite(rs.getString("website"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(Subscription s) {
        String sql = "INSERT INTO subscriptions (name, cost, currency, start_date, next_payment_date, billing_cycle, category, active, description, website) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setDouble(2, s.getCost() != null ? s.getCost().doubleValue() : 0.0);
            ps.setString(3, s.getCurrency());
            ps.setString(4, s.getStartDate() != null ? s.getStartDate().toString() : null);
            ps.setString(5, s.getNextPaymentDate() != null ? s.getNextPaymentDate().toString() : null);
            ps.setInt(6, s.getBillingCycle());
            ps.setString(7, s.getCategory() != null ? s.getCategory().getDisplayName() : null);
            ps.setInt(8, s.isActive() ? 1 : 0);
            ps.setString(9, s.getDescription());
            ps.setString(10, s.getWebsite());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Subscription s) {
        String sql = "UPDATE subscriptions SET name=?, cost=?, currency=?, start_date=?, next_payment_date=?, billing_cycle=?, category=?, active=?, description=?, website=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setDouble(2, s.getCost() != null ? s.getCost().doubleValue() : 0.0);
            ps.setString(3, s.getCurrency());
            ps.setString(4, s.getStartDate() != null ? s.getStartDate().toString() : null);
            ps.setString(5, s.getNextPaymentDate() != null ? s.getNextPaymentDate().toString() : null);
            ps.setInt(6, s.getBillingCycle());
            ps.setString(7, s.getCategory() != null ? s.getCategory().getDisplayName() : null);
            ps.setInt(8, s.isActive() ? 1 : 0);
            ps.setString(9, s.getDescription());
            ps.setString(10, s.getWebsite());
            ps.setLong(11, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM subscriptions WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}