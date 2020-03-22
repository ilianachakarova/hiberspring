package hiberspring.service;

import hiberspring.domain.dtos.employees_dtos.EmployeeImportDto;
import hiberspring.domain.dtos.employees_dtos.EmployeeImportRootDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Employee;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.BranchRepository;
import hiberspring.repository.EmployeeCardRepository;
import hiberspring.repository.EmployeeRepository;
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
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final BranchRepository branchRepository;
    private final EmployeeCardRepository employeeCardRepository;

    private final static String EMPLOYEE_XML_FILE =
            "C:\\Users\\user l\\Downloads\\Hiberspring Inc._Skeleton\\src\\main\\resources\\files\\employees.xml";

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ModelMapper modelMapper, FileUtil fileUtil, ValidationUtil validationUtil, XmlParser xmlParser, BranchRepository branchRepository, EmployeeCardRepository employeeCardRepository) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.branchRepository = branchRepository;
        this.employeeCardRepository = employeeCardRepository;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() > 0;
    }

    @Override
    public String readEmployeesXmlFile() throws IOException {
        return this.fileUtil.readFile(EMPLOYEE_XML_FILE);
    }

    @Override
    public String importEmployees() throws JAXBException, FileNotFoundException {
        StringBuilder importResult = new StringBuilder();
        EmployeeImportRootDto employeeImportRootDto = this.xmlParser.parseXml(EmployeeImportRootDto.class, EMPLOYEE_XML_FILE);
        List<EmployeeImportDto> employeeImportDtos = employeeImportRootDto.getEmployees();
        for (EmployeeImportDto employeeImportDto : employeeImportDtos) {
            Employee employee = this.employeeRepository.
                    findByFirstNameAndLastName(employeeImportDto.getFirstName(),
                            employeeImportDto.getLastName()).orElse(null);
            if (employee == null) {
                EmployeeCard card = this.employeeCardRepository.findByNumber(employeeImportDto.getCard()).orElse(null);
                Branch branch = this.branchRepository.findByName(employeeImportDto.getBranch()).orElse(null);
                if (this.validationUtil.isValid(employeeImportDto) && card != null && branch != null) {
                        List<EmployeeCard> cards =
                                this.employeeRepository.findAll().stream().map(Employee::getCard).collect(Collectors.toList());
                        if (!cards.contains(card)|| cards == null) {

                            employee = this.modelMapper.map(employeeImportDto, Employee.class);
                            employee.setCard(card);
                            employee.setBranch(branch);
                            this.employeeRepository.saveAndFlush(employee);
                    importResult.append("Successfully imported Employee - ").append(employee.getFirstName()).append(" ")
                            .append(employee.getLastName()).append(System.lineSeparator());
                        }


                }else{
                    importResult.append("Invalid data").append(System.lineSeparator());
                }

            } else {
                importResult.append("Error: Alredy in DB").append(System.lineSeparator());
            }
        }
        return importResult.toString().trim();
    }

    @Override
    public String exportProductiveEmployees() {
        List<Employee> employees = this.employeeRepository.findAllByBranchesWithAtLeastOneProduct();
        return this.setOutputText(employees);

    }

    private String setOutputText(List<Employee> employees) {
        StringBuilder resultText = new StringBuilder();
        for (Employee employee : employees) {
            resultText.append("Name: ").
                    append(employee.getFirstName()).append(" ").append(employee.getLastName())
                    .append(System.lineSeparator());
            resultText.append("Position: ").append(employee.getPosition()).append(System.lineSeparator());
            resultText.append("Card Number: ").append(employee.getCard().getNumber()).append(System.lineSeparator());
            resultText.append("-------------------------------------------").append(System.lineSeparator());
        }
        return resultText.toString().trim();
    }
}
