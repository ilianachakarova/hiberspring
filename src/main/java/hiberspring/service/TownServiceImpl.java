package hiberspring.service;

import com.google.gson.Gson;
import hiberspring.domain.dtos.TownImportDto;
import hiberspring.domain.entities.Town;
import hiberspring.repository.TownRepository;
import hiberspring.util.FileUtil;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@Service
public class TownServiceImpl implements TownService {
    private final TownRepository townRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final Gson gson;
    private final ModelMapper modelMapper;
        private static final String TOWNS_JSON_FILE = "src/main/resources/files/towns.json";

    @Autowired
    public TownServiceImpl(TownRepository townRepository, FileUtil fileUtil, ValidationUtil validationUtil, Gson gson, ModelMapper modelMapper) {
        this.townRepository = townRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.gson = gson;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean townsAreImported() {
        return this.townRepository.count()>0;
    }

    @Override
    public String readTownsJsonFile() throws IOException {
        return this.fileUtil.readFile(TOWNS_JSON_FILE);
    }

    @Override
    public String importTowns(String townsFileContent) throws FileNotFoundException {
        StringBuilder importResult = new StringBuilder();
        TownImportDto[] townImportDtos = this.gson.fromJson(townsFileContent,TownImportDto[].class);
        System.out.println();
        Arrays.stream(townImportDtos).forEach(townImportDto -> {
            if(this.validationUtil.isValid(townImportDto)){
                Town town = this.townRepository.findByName(townImportDto.getName()).orElse(null);
                if(town == null){
                    town = this.modelMapper.map(townImportDto,Town.class);

                    this.townRepository.saveAndFlush(town);
                    importResult.append("Successfully imported Town - ")
                            .append(town.getName()).append(System.lineSeparator());

                }else {
                    importResult.append("Error: Invalid data - already in DB.").append(System.lineSeparator());
                }
            }else {
                importResult.append("Error: Invalid data.").append(System.lineSeparator());
            }
        });
        return importResult.toString().trim();
    }
}
