package com.example.demo.service;
import com.example.demo.model.Order;
import com.example.demo.model.Product;
import com.example.demo.repository.mongo.OrderRepository;
import com.example.demo.repository.r2dbc.CustomerRepository;
import com.example.demo.repository.r2dbc.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;

    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Mono<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public Mono<Order> createOrder(Order order) {
        return customerRepository.findById(order.getCustomer().getId())
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")))
                .flatMap(customer -> {
                    order.setCustomer(customer);
                    return Flux.fromIterable(order.getProducts())
                            .flatMap(product -> productRepository.findById(product.getId()))
                            .collectList()
                            .flatMap(products -> {
                                if (products.size() != order.getProducts().size()) {
                                    return Mono.error(new RuntimeException("One or more products not found"));
                                }
                                order.setProducts(products);
                                double total = products.stream()
                                        .mapToDouble(Product::getPrice)
                                        .sum();
                                order.setTotal(total);
                                return orderRepository.save(order);
                            });
                });
    }

    public Mono<Void> deleteOrder(String id) {
        return orderRepository.deleteById(id);
    }

    public Mono<Order> updateOrder(String id, Order updatedOrder) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(existingOrder -> customerRepository.findById(updatedOrder.getCustomer().getId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")))
                        .flatMap(customer -> {
                            existingOrder.setCustomer(customer);
                            return Flux.fromIterable(updatedOrder.getProducts())
                                    .flatMap(product -> productRepository.findById(product.getId()))
                                    .collectList()
                                    .flatMap(products -> {
                                        if (products.size() != updatedOrder.getProducts().size()) {
                                            return Mono.error(new RuntimeException("One or more products not found"));
                                        }
                                        existingOrder.setProducts(products);
                                        double total = products.stream()
                                                .mapToDouble(Product::getPrice)
                                                .sum();
                                        existingOrder.setTotal(total);
                                        return orderRepository.save(existingOrder);
                                    });
                        }));
    }

}
