package com.logistics.dispatch.service;

import com.logistics.dispatch.model.Order;
import com.logistics.dispatch.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order order) {
        order.setStatus("PENDING");
        if (order.getWeight() == null) order.setWeight(0.0);
        if (order.getVolume() == null) order.setVolume(0.0);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 24);
        order.setExpectedArrivalTime(cal.getTime());
        
        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(String id, String status) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            if ("COMPLETED".equals(status)) {
                order.setActualArrivalTime(new Date());
            }
            return orderRepository.save(order);
        }
        return null;
    }

    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    public boolean deleteOrder(String id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void initMockData() {
        String[] regions = {"华东区", "华北区", "华南区", "西南区", "华中区"};
        String[] goods = {"电子产品", "服装", "食品", "家具", "建材"};
        String[] addresses = {"上海", "北京", "广州", "深圳", "杭州", "南京", "武汉", "成都"};

        for (int i = 0; i < 15; i++) {
            Order order = new Order();
            order.setGoodsName(goods[i % goods.length] + (i + 1));
            order.setWeight(100.0 + Math.random() * 900);
            order.setVolume(1.0 + Math.random() * 10);
            order.setRegion(regions[i % regions.length]);
            order.setStartAddress(addresses[i % addresses.length]);
            order.setEndAddress(addresses[(i + 3) % addresses.length]);
            order.setStatus("PENDING");
            orderRepository.save(order);
        }
    }
}
