const csvService = require("../../src/services/csvService");
const fs = require("fs");

jest.mock('fs')
describe("csvService", ()=>{
    beforeEach(()=>{
       fs.readFileSync.mockReturnValue("hour,susceptible,exposed,infected,hospitalized,recovered,deceased\n" +
           "1,9999,1,0,0,0,0\n" +
           "2,9999,1,0,0,0,0")
    })

    it("should get data from csv", ()=>{
        const data = csvService.getData();
        expect(data).toEqual({columns: {exposed: [2, 3], "hour": [1, 2]}})
    })
    it("should get headers from csv", ()=>{
        const data = csvService.getHeaders();
        expect(data).toEqual({headers: ["hour", "susceptible", "exposed", "infected", "hospitalized", "recovered", "deceased"]});
    })
})