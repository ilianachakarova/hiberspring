package hiberspring.service;

import hiberspring.domain.dtos.product_dtos.ProductImportDto;
import hiberspring.domain.dtos.product_dtos.ProductImportRootDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Product;
import hiberspring.repository.BranchRepository;
import hiberspring.repository.ProductRepository;
import hiberspring.util.FileUtil;
import hiberspring.util.ValidationUtil;
import hiberspring.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final BranchRepository branchRepository;

    private final static String PRODUCT_XML_FILE =
            "C:\\Users\\user l\\Downloads\\Hiberspring Inc._Skeleton\\src\\main\\resources\\files\\products.xml";
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper, FileUtil fileUtil, ValidationUtil validationUtil, XmlParser xmlParser, BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.branchRepository = branchRepository;
    }

    @Override
    public Boolean productsAreImported() {
        return this.productRepository.count()>0;
    }

    @Override
    public String readProductsXmlFile() throws IOException {
        return this.fileUtil.readFile(PRODUCT_XML_FILE);
    }

    @Override
    public String importProducts() throws JAXBException, FileNotFoundException {
        StringBuilder importResult = new StringBuilder();
        ProductImportRootDto productImportRootDto = this.xmlParser.parseXml(ProductImportRootDto.class, PRODUCT_XML_FILE);
        List<ProductImportDto>productImportDtos = productImportRootDto.getProducts();
        for (ProductImportDto productImportDto : productImportDtos) {
            Product product = this.productRepository.findByName(productImportDto.getName()).orElse(null);
            if(product == null){
                Branch branch = this.branchRepository.findByName(productImportDto.getBranch()).orElse(null);
                if(this.validationUtil.isValid(productImportDto)&&branch!=null){
                    product = this.modelMapper.map(productImportDto,Product.class);
                    product.setBranch(branch);
                    this.productRepository.saveAndFlush(product);
                    importResult.append("Successfully imported Product - ").append(product.getName()).append(System.lineSeparator());
                }
            }else {
                importResult.append("Error: Already in DB").append(System.lineSeparator());
            }
        }
        return importResult.toString().trim();
    }
}
