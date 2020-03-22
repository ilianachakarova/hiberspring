package hiberspring.service;

import com.google.gson.Gson;
import hiberspring.domain.dtos.BranchImportDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Town;
import hiberspring.repository.BranchRepository;
import hiberspring.repository.TownRepository;
import hiberspring.util.FileUtil;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final TownRepository townRepository;
    private final Gson gson;

    private final static String BRANCH_JSON_FILE =
            "C:\\Users\\user l\\Downloads\\Hiberspring Inc._Skeleton\\src\\main\\resources\\files\\branches.json";

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, FileUtil fileUtil, ModelMapper modelMapper, ValidationUtil validationUtil, TownRepository townRepository, Gson gson) {
        this.branchRepository = branchRepository;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.townRepository = townRepository;
        this.gson = gson;
    }

    @Override
    public Boolean branchesAreImported() {
        return this.branchRepository.count() > 0;
    }

    @Override
    public String readBranchesJsonFile() throws IOException {
        return this.fileUtil.readFile(BRANCH_JSON_FILE);
    }

    @Override
    public String importBranches(String branchesFileContent) {
        StringBuilder importResult = new StringBuilder();
        BranchImportDto []branchImportDtos =
                this.gson.fromJson(branchesFileContent, BranchImportDto[].class);

        Arrays.stream(branchImportDtos).forEach(branchImportDto -> {
            Branch branch = this.branchRepository.findByName(branchImportDto.getName()).orElse(null);
            if(branch == null){
                Town town = this.townRepository.findByName(branchImportDto.getTown()).orElse(null);
                if(this.validationUtil.isValid(branchImportDto)&&town!=null){
                    branch = this.modelMapper.map(branchImportDto,Branch.class);
                    branch.setTown(town);

                    this.branchRepository.saveAndFlush(branch);
                    importResult.append("Successfully imported Branch - ").append(branch.getName());
                }else {
                    importResult.append("Error: Invalid data");
                }
            }else {
                importResult.append("Error: Invalid data - entity already in DB").append(System.lineSeparator());
            }
        });
        return importResult.toString().trim();
    }
}
