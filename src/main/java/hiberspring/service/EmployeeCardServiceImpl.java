package hiberspring.service;

import com.google.gson.Gson;
import hiberspring.domain.dtos.EmployeeCardDto;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeCardRepository;
import hiberspring.util.FileUtil;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmployeeCardServiceImpl implements EmployeeCardService{
    private final EmployeeCardRepository employeeCardRepository;
    private final ModelMapper modelMapper;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final Gson gson;

    private final static String EMPLOYEE_CARD_JSON_FILE =
            "C:\\Users\\user l\\Downloads\\Hiberspring Inc._Skeleton\\src\\main\\resources\\files\\employee-cards.json";

    @Autowired
    public EmployeeCardServiceImpl(EmployeeCardRepository employeeCardRepository, ModelMapper modelMapper, FileUtil fileUtil, ValidationUtil validationUtil, Gson gson) {
        this.employeeCardRepository = employeeCardRepository;
        this.modelMapper = modelMapper;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }

    @Override
    public Boolean employeeCardsAreImported() {
        return this.employeeCardRepository.count()>0;
    }

    @Override
    public String readEmployeeCardsJsonFile() throws IOException {
        return this.fileUtil.readFile(EMPLOYEE_CARD_JSON_FILE);
    }

    @Override
    public String importEmployeeCards(String employeeCardsFileContent) {
        StringBuilder importResult = new StringBuilder();
        EmployeeCardDto [] employeeCardDto = this.gson.fromJson(employeeCardsFileContent,EmployeeCardDto[].class);
        for (EmployeeCardDto cardDto : employeeCardDto) {
            EmployeeCard employeeCard = this.employeeCardRepository.findByNumber(cardDto.getNumber()).orElse(null);
            if(employeeCard == null){
                if(this.validationUtil.isValid(cardDto)){
                    employeeCard = this.modelMapper.map(cardDto, EmployeeCard.class);
                    this.employeeCardRepository.saveAndFlush(employeeCard);
                }else {
                    importResult.append("Invalid data").append(System.lineSeparator());
                }
            }else {
                importResult.append("Error: Invalid data. Entity already in DB");
            }
        }
        return importResult.toString().trim();
    }
}
