package com.jayesh.ecom.service;

import com.jayesh.ecom.exceptions.APIException;
import com.jayesh.ecom.exceptions.ResourceNotFoundException;
import com.jayesh.ecom.model.Category;
import com.jayesh.ecom.model.Product;
import com.jayesh.ecom.payload.ProductDTO;
import com.jayesh.ecom.payload.ProductResponse;
import com.jayesh.ecom.repository.CategoryRepository;
import com.jayesh.ecom.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {

        // 1. Validate category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        // 2. Check if product already exists in this category
        List<Product> products = category.getProducts();
        if (products.stream().
                anyMatch(p -> p.getProductName().equals(productDTO.getProductName()))) {
            throw new APIException("Product already exists");
        }

        // 3. Map DTO to entity
        Product product = modelMapper.map(productDTO, Product.class);

        // 4. Assign defaults
        product.setImage("default.png");
        product.setCategory(category);
        product.setSpecialPrice(calculateSpecialPrice(product.getPrice(), product.getDiscount()));

        // 5. Save
        Product savedProduct = productRepository.save(product);

        // 6. Return DTO
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    private double calculateSpecialPrice(double price, double discount) {
        return price - (price * discount / 100.0);
    }

    @Override
    public ProductResponse getAllProducts(
            Integer pageNumber, Integer pageSize, String sortBy, String sortOrder
    ) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, "productId")
                : Sort.by(Sort.Direction.DESC, "productId");

        Pageable pageDetails = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);
        List<Product> products = productPage.getContent();
        if (products.isEmpty()) {
            throw new APIException("No products found");
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return getProductResponse(productPage, productDTOs);
    }

    @Override
    public ProductResponse searchByCategory(
            Long categoryId, Integer pageNumber, Integer pageSize, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, "productId")
                : Sort.by(Sort.Direction.DESC, "productId");

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return getProductResponse(productPage, productDTOs);
    }

    private ProductResponse getProductResponse(Page<Product> productPage, List<ProductDTO> productDTOs) {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOs);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, "productId")
                : Sort.by(Sort.Direction.DESC, "productId");

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);

        Page<Product> products = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageable);

        List<ProductDTO> productDTOs = products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return getProductResponse(products, productDTOs);
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        Category category = existingProduct.getCategory();
        List<Product> products = category.getProducts();
        if (products.stream().anyMatch(p -> p.getProductName().equals(productDTO.getProductName()))) {
            throw new APIException("Product already exists with same name");
        }

        existingProduct.setProductName(productDTO.getProductName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setQuantity(existingProduct.getQuantity());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDiscount(productDTO.getDiscount());

        double specialPrice = existingProduct.getPrice() - ((existingProduct.getDiscount() * 0.01) * existingProduct.getPrice());
        existingProduct.setSpecialPrice(specialPrice);

        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        // Upload image to server and
        // Get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        // Updating the new file name to the product
        product.setImage(fileName);

        // Save the updated product
        Product updatedProduct = productRepository.save(product);

        // return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }
}
