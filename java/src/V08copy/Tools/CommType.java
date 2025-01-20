package V08copy.Tools;

public enum CommType {
    /// tower->robot: need defenders to help current tower defend from attackers
    WantDefenders,
    /// tower->robot: target enemies in tower vicinity
    TargetEnemy,
    /// tower->both : have a frontline that we want backline units to help out with
    ReinforceFront,
    /// tower->tower: comm telling each other what kinda tower they are, and spread news about paint towers
    CommunicateType,
    /// tower->robot: tell a robot the nearest paint tower to this one
    NearbyPaintTower,
    /// robot->tower: ask for the closest paint tower this one knows of
    RequestPaintTower,
    
    /// tower->tower: request PAINT TOWERS for soldiers for rebuilding
    RequestSoldiers,
    /// tower->robot: provides a location to rebuild at
    RebuildTower,
}
